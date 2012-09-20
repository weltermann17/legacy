package com.ibm.haploid

package dx

package engine

package event

import java.nio.file.Path

import com.ibm.haploid.core.service.Result
import com.ibm.haploid.dx.engine.domain.binding.{xmlAttribute, xmlJavaTypeAdapter, xmlElement, xmlAnyElement}
import com.ibm.haploid.dx.engine.domain.binding.{StringOptionAdapter, ResultAdapter, AnyAdapter, ActorPathAdapter}
import com.ibm.haploid.dx.engine.domain.flow.ExecutionDetail
import com.ibm.haploid.dx.engine.domain.marshalling.Marshaled

import akka.actor.{ActorRef, ActorPath}
import core.util.Uuid.newUuid
import core.util.time.now
import domain.operating.{OperationResultDetail, OperationDetail}
import domain.{TaskDetail, JobDetail}
import javax.xml.bind.annotation.{XmlType, XmlRootElement, XmlAttribute}

/**
 *
 */
sealed trait Event {

  val id = newUuid.toString

  val created = now

  override def equals(other: Any) = other match {
    case e: Event ⇒ id == e.id
    case _ ⇒ false
  }

  override def hashCode = id.toString.hashCode

}

/**
 *
 */
sealed trait TransientEvent extends Event

/**
 *
 */
@XmlType(propOrder = Array("id", "create"))
sealed trait PersistentEvent

  extends Event {

  def online = _online

  private[engine] def redo = _online = false

  private[engine] def relaunch = _online = true

  private[this] var _online = true

  @XmlAttribute(required = true) def getId = id

  @XmlAttribute(required = true) def getCreated = created

}

@XmlRootElement(name = "receiver-event")
@XmlType(propOrder = Array("receiver", "event"))
case class ReceiverEvent(

  @xmlJavaTypeAdapter(classOf[ActorPathAdapter]) receiver: ActorPath,

  @xmlAnyElement(lax = true) event: PersistentEvent)

  extends Marshaled {

  def this() = this(null.asInstanceOf[ActorPath], null.asInstanceOf[PersistentEvent])

  def this(path: String, event: PersistentEvent) = this(ActorPath.fromString("akka://default/user" + path), event)

}

object ReceiverEvent {

  def apply[E <: PersistentEvent](path: String, event: E) = new ReceiverEvent(path, event)

  def apply[E <: PersistentEvent](receiver: ActorRef, event: E) = new ReceiverEvent(receiver.path, event)

}

/**
 *
 */
sealed trait JobEvent extends PersistentEvent

case object JobEvent extends JobEvent

/**
 *
 */
@XmlRootElement(name = "job-create-event")
@XmlType(propOrder = Array("counter", "detail", "trigger"))
case class JobCreate(

  @xmlElement(required = true) detail: JobDetail,

  @xmlJavaTypeAdapter(classOf[StringOptionAdapter]) trigger: Option[String] = None)

  extends JobEvent 
  
  with ExecutionCreate {

  def this() = this(null, None)

  val name = id

  @xmlAttribute(required = true) def getCounter = _counter

  private[engine] var _counter = -1
  
}

/**
 *
 */
sealed trait TaskEvent extends PersistentEvent

/**
 * sent from Task to its parent Job
 */
@XmlRootElement(name = "task-result-event")
case class TaskResult(

  @xmlJavaTypeAdapter(classOf[ResultAdapter]) val result: Result[Any])(implicit 
      
  @xmlJavaTypeAdapter(classOf[ActorPathAdapter]) val sender: ActorPath)

  extends ExecutionResult

  with TaskEvent {

  def this() = this(null)(null)

}

/**
 * sent from Task to its parent Job
 */
@XmlRootElement(name = "job-result-event")
case class JobResult(

  @xmlJavaTypeAdapter(classOf[ResultAdapter]) val result: Result[Any])(implicit 
      
  @xmlJavaTypeAdapter(classOf[ActorPathAdapter]) val sender: ActorPath)

  extends ExecutionResult

  with TaskEvent {

  def this() = this(null)(null)

}

/**
 *
 */
@XmlRootElement(name = "task-create-event")
@XmlType(propOrder = Array("name", "job", "detail"))
case class TaskCreate(

  @xmlElement(required = true) job: JobCreate,

  @xmlAttribute(required = true) name: String,

  @xmlElement(required = true) detail: TaskDetail)

  extends TaskEvent

  with ExecutionCreate {

  def this() = this(null, null, null)

}

sealed trait ExecutionEvent extends PersistentEvent

trait ExecutionResult

  extends ExecutionEvent {

  val result: Result[Any]

  val sender: ActorPath

}

object ExecutionResult {

  def unapply(er: ExecutionResult): Option[Result[Any]] = {
    er match {
      case DefaultExecutionResult(result) ⇒
        Some(result)
      case TaskOperationResult(result) ⇒
        Some(result)
      case TaskResult(result) ⇒
        Some(result)
      case JobResult(result) ⇒
        Some(result)
      case _ ⇒
        None
    }
  }

}

@XmlRootElement(name = "default-execution-result-event")
case class DefaultExecutionResult(

  @xmlJavaTypeAdapter(classOf[ResultAdapter]) val result: Result[Any])(implicit 
      
  @xmlJavaTypeAdapter(classOf[ActorPathAdapter]) val sender: ActorPath)

  extends ExecutionResult {

  def this() = this(null)(null)

}

sealed trait ExecutionCreate extends ExecutionEvent {

  val name: String

  val detail: ExecutionDetail

}

/**
 *
 */
@XmlRootElement(name = "execution-start-event")
case class ExecutionStartEvent() extends ExecutionEvent

/**
 *
 */
sealed trait OperationEvent extends PersistentEvent

/**
 *
 */
@XmlRootElement(name = "operation-create-event")
@XmlType(propOrder = Array("task", "name", "detail"))
case class OperationCreate(

  @xmlElement(required = true) task: TaskCreate,

  @xmlAttribute(required = true) name: String,

  @xmlAnyElement(lax = true) detail: OperationDetail)

  extends OperationEvent with ExecutionCreate {

  def this() = this(null, null, null)

}

@XmlRootElement(name = "reset-event")
case class Reset(

  @xmlElement(required = true) force: Boolean)

  extends PersistentEvent {

  def this() = this(false)

}

@XmlRootElement(name = "reset-ack-event")
case class ResetAck(

  @xmlElement(required = true) force: Boolean)

  extends PersistentEvent {

  def this() = this(false)

}

/**
 * sent from Operator to Operation
 */
@XmlRootElement(name = "operation-result-event")
@XmlType(propOrder = Array("detail"))
case class OperationResult(

  @xmlElement detail: OperationResultDetail)

  extends OperationEvent {

  def this() = this(null)

}

/**
 * sent from Operation to its parent Task
 */
@XmlRootElement(name = "task-operation-result-event")
case class TaskOperationResult(

  @xmlJavaTypeAdapter(classOf[ResultAdapter]) val result: Result[Any])(implicit 
      
  @xmlJavaTypeAdapter(classOf[ActorPathAdapter]) val sender: ActorPath)

  extends ExecutionResult

  with OperationEvent {

  def this() = this(null)(null)
}

/**
 * Sent by a parent Task on one of its Operations
 */
@XmlRootElement(name = "execute")
@XmlType
case class Execute(
    
		@xmlJavaTypeAdapter(classOf[AnyAdapter]) input: Any)

  extends PersistentEvent {

  def this() = this(null)

}

/**
 * Transient events to manage the engine components.
 */
case object Redo extends TransientEvent

case object Exists extends TransientEvent

case class Collect(collector: ActorRef, depth: Option[Int] = None) extends TransientEvent

case class CollectResponse(data: Any, count: Int) extends TransientEvent

/**
 * Transient events for operation handling.
 */

/**
 * Sent from an Operation to its Operator
 */
case class OperationExecute(operation: OperationCreate, basedirectory: Path, input: Any)

  extends TransientEvent

