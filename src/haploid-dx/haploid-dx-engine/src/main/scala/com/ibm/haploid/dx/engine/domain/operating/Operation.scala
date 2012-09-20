package com.ibm.haploid

package dx

package engine

package domain

package operating

import java.nio.file.Paths

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlElement, XmlAttribute }

import akka.actor.actorRef2Scala
import akka.actor.ActorPath

import com.ibm.haploid.core.service.Result
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.domain.flow.{ Succeeded, IncompleteData, Failed, ExecutionResultDetail, ExecutionDetail, Executable, CompletedData }
import com.ibm.haploid.dx.engine.domain.Active
import com.ibm.haploid.dx.engine.event.{ OperationExecute, OperationCreate, Collect }

import core.inject.BindingModule
import core.service.{ Success, Failure }

import domain.DomainObject
import event.{ TaskOperationResult, OperationResult }

/**
 * Details of an Operation (the input).
 */
abstract class OperationDetail(val operator: String) extends ExecutionDetail {

  private def this() = this(null)

}

/**
 * Details of an Operation result (the output).
 */
@XmlRootElement(name = "operation-result-detail")
@XmlType(propOrder = Array("success", "durationinmilliseconds", "result", "reason", "console", "logfile", "internallogging", "stacktrace"))
case class OperationResultDetail(

  @xmlJavaTypeAdapter(classOf[ResultAdapter]) result: Result[Any],

  @xmlJavaTypeAdapter(classOf[CDataAdapter]) reason: String,

  @xmlJavaTypeAdapter(classOf[CDataAdapter]) console: String,

  @xmlJavaTypeAdapter(classOf[CDataAdapter]) logfile: String,

  @xmlJavaTypeAdapter(classOf[CDataAdapter]) internallogging: String,

  @xmlJavaTypeAdapter(classOf[CDataAdapter]) stacktrace: String,

  @xmlAttribute(required = true) durationinmilliseconds: Long)

  extends ExecutionResultDetail {

  def this() = this(null, null, null, null, null, null, -1L)

  @XmlAttribute(required = true) def getSuccess = result match {
    case Success(_) ⇒ true
    case Failure(_) ⇒ false
  }
  
}

/**
 *
 */
@XmlRootElement(name = "operation")
@XmlType(propOrder = Array("id", "status", "name", "created", "completed", "basedirectory", "task", "job", "detail", "result"))
case class Operation(

  event: OperationCreate,

  @xmlAttribute(required = true) name: String,

  @xmlAttribute(required = true) status: String,

  @xmlAttribute(required = true) basedirectory: String,

  @xmlAttribute completed: Long,

  @xmlElement(nillable = true) result: OperationResultDetail)

  extends DomainObject {

  private def this() = this(null, null, null, null, -1L, null)

  @XmlAttribute(required = true) val id = event.id

  @XmlAttribute(required = true) val task = event.task.id

  @XmlAttribute(required = true) val created = event.created

  @XmlAttribute(required = true) val job = event.task.job.id

  @XmlElement(required = true) val detail = event.detail

}

/**
 *
 */
class OperationFSM(

  val operation: OperationCreate)(implicit bindingmodule: BindingModule)

  extends Executable {

  protected[this] lazy val execution = operation

  protected[this] lazy val parent = Some(context.system.actorFor(self.path.parent.parent))

  private[this] lazy val operator = ActorPath.fromString("akka://default/user/engine/operators/" + operation.detail.operator)

  private[this] lazy val basedirectory = {
    Paths.get(rootdirectory.resolve(self.path.toString.replace("akka://default/user/", "")).toAbsolutePath.toString
      .replace(operation.task.job.id, operation.task.job._counter.toString)
      .replace("operations", "o")
      .replace("subtasks", "s")
      .replace("tasks", "t"))
  }

  override val log = core.newLogger(operation.toString)

  execute {
    case input: Any ⇒
      if (isOnline) {
        log.debug("Online 'OperationExecute' to operator " + operator)
        context.system.actorFor(operator) ! OperationExecute(operation, basedirectory, input)
      }
      goto(Active)
  }

  relaunchWithInput {
    case (_, input) ⇒
      log.debug("Relaunch 'OperationExecute' to operator " + operator)
      context.system.actorFor(operator) ! OperationExecute(operation, basedirectory, input)
      goto(Active) using initialData
  }

  complete {
    case OperationResultDetail(result, _, _, _, _, _, _) ⇒
      TaskOperationResult(result)
  }

  whenUnhandled {
    case Event(event @ OperationResult(result), IncompleteData(operation, _)) ⇒ result match {

      case details @ OperationResultDetail(succ @ Success(_), _, _, _, _, _, _) ⇒
        goto(Succeeded) using CompletedData(operation, event.created, details)

      case details @ OperationResultDetail(fail @ Failure(_), _, _, _, _, _, _) ⇒
        goto(Failed) using CompletedData(operation, event.created, details)

    }

    case Event(Collect(collector, _), IncompleteData(operation @ OperationCreate(_, _, _), _)) ⇒
      collector ! Operation(operation, self.path.name, stateName.toString.toLowerCase, basedirectory.toString, -1L, null)
      stay
    case Event(Collect(collector, _), CompletedData(operation @ OperationCreate(_, _, _), completed, details @ OperationResultDetail(_, _, _, _, _, _, _))) ⇒
      collector ! Operation(operation, self.path.name, stateName.toString.toLowerCase, basedirectory.toString, completed, details)
      stay

  }

  initialize

}

