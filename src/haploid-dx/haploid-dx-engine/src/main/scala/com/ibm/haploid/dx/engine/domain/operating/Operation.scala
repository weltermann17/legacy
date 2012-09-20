package com.ibm.haploid

package dx

package engine

package domain

package operating

import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlElement, XmlAttribute }

import akka.actor.ActorPath

import com.ibm.haploid.dx.engine.domain.DomainObjectFSM
import com.ibm.haploid.dx.engine.event.OperationCreate

import core.inject.BindingModule
import core.service._
import core.util.text.stackTraceToBase64

import domain.{ Idle, DomainObjectState, DomainObjectFSM, DomainObject }
import event.{ OperationResult, OperationExecute, OperationCreate, Execute, Collect }
import binding._

/**
 * States of a Operation.
 */
sealed trait OperationState extends DomainObjectState

case object OperationSucceeded extends OperationState
case object OperationFailed extends OperationState
case object OperationNotApplicable extends OperationState
case object OperationArchived extends OperationState

/**
 * Details of an Operation (the input).
 */
abstract class OperationDetail(

  val operator: String)

  extends DomainObject {

  def this() = this(null)

}

/**
 * Details of an Operation result (the output).
 */
abstract class OperationResultDetail(success: Boolean)

  extends DomainObject {

  def this() = this(false)

  @XmlAttribute(required = true) def getSuccess = success

}

/**
 * Simple output wrapper
 */
@XmlType(name = "simple-operation-result-detail")
case class SimpleOperationResultDetail(

  private val success: Boolean,

  @xmlJavaTypeAdapter(classOf[CDataAdapter]) result: String)

  extends OperationResultDetail(success) {

  def this() = this(false, null)

}

/**
 * In case an operation failed.
 */
case class OperationFailed(details: OperationResultDetail)

  extends Exception

/**
 * Data of a Operation.
 */
sealed trait OperationData

case class OperationIncomplete(create: OperationCreate) extends OperationData

case class OperationCompleted(create: OperationCreate, completed: Long, details: OperationResultDetail) extends OperationData

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

  val operation: OperationCreate)(

    implicit bindingmodule: BindingModule)

  extends DomainObjectFSM[OperationData] {

  private[this] lazy val operator = ActorPath.fromString("akka://default/user/engine/operators/" + operation.detail.operator)

  private[this] lazy val basedirectory = rootdirectory.resolve(self.path.toString.replace("akka://default/user/", ""))

  startWith(Idle, OperationIncomplete(operation))

  when(Idle) {
    case Event(Execute, _) ⇒
      context.system.actorFor(operator) ! OperationExecute(operation, basedirectory)
      goto(Active)
  }

  when(OperationSucceeded) {
    case Event((), _) ⇒ stay
  }

  when(OperationFailed) {
    case Event((), _) ⇒ stay
  }

  whenUnhandled {

    case Event(event @ OperationResult(result), OperationIncomplete(operation)) ⇒ result match {

      case Success(details) if details.isInstanceOf[OperationResultDetail] =>
        goto(OperationSucceeded) using OperationCompleted(operation, event.created, details.asInstanceOf[OperationResultDetail])

      case Success(details) =>
        goto(OperationSucceeded) using OperationCompleted(operation, event.created, SimpleOperationResultDetail(true, details.toString))

      case Failure(OperationFailed(details)) =>
        goto(OperationFailed) using OperationCompleted(operation, event.created, details)

      case Failure(throwable) =>
        goto(OperationFailed) using OperationCompleted(operation, event.created, SimpleOperationResultDetail(false, stackTraceToBase64(throwable)))
    }

    case Event(Collect(collector), OperationIncomplete(operation)) ⇒
      collector ! Operation(operation, self.path.name, stateName.toString.toLowerCase, basedirectory.toString, -1L, null)
      stay

    case Event(Collect(collector), OperationCompleted(operation, completed, details)) ⇒
      collector ! Operation(operation, self.path.name, stateName.toString.toLowerCase, basedirectory.toString, completed, details)
      stay

  }

  initialize

}


