package com.ibm.haploid

package dx

package engine

package event

import java.nio.file.Path

import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlElement, XmlAttribute }

import akka.actor.{ ActorRef, ActorPath }

import com.ibm.haploid.core.service.Result
import com.ibm.haploid.dx.engine.domain.marshalling.Marshaled

import core.service.{ Success, Failure }
import core.util.Uuid.newUuid
import core.util.text.stackTraceToBase64
import core.util.time.now

import domain.operating.{ SimpleOperationResultDetail, OperationResultDetail, OperationFailed, OperationDetail }
import domain._
import domain.binding._

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

  private[this] var _online = true

  @XmlAttribute(required = true) def getId = id

  @XmlAttribute(required = true) def getCreated = created

}

@XmlRootElement(name = "receiver-event")
@XmlType(propOrder = Array("receiver", "event"))
case class ReceiverEvent(

  receiver: ActorPath,

  @xmlAnyElement event: PersistentEvent)

  extends Marshaled {

  def this() = this(null.asInstanceOf[ActorPath], null.asInstanceOf[PersistentEvent])

  def this(path: String, event: PersistentEvent) = this(ActorPath.fromString("akka://default/user" + path), event)

  @XmlAttribute(required = true) def getReceiver = receiver.toString

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
@XmlType(propOrder = Array("job", "detail"))
case class JobCreate(

  @xmlAttribute(required = true) job: Class[_ <: JobFSM],

  @xmlElement(required = true) detail: JobDetail)

  extends JobEvent {

  def this() = this(null, null)

}

/**
 *
 */
sealed trait TaskEvent extends PersistentEvent

case object TaskEvent extends TaskEvent

/**
 *
 */
@XmlRootElement(name = "task-create-event")
@XmlType(propOrder = Array("task", "name", "job", "detail"))
case class TaskCreate(

  @xmlAttribute(required = true) task: Class[_ <: TaskFSM],

  @xmlElement(required = true) job: JobCreate,

  @xmlAttribute(required = true) name: String,

  @xmlElement(required = true) detail: TaskDetail)

  extends TaskEvent {

  def this() = this(null, null, null, null)

}

/**
 *
 */
sealed trait OperationEvent extends PersistentEvent

case object OperationEvent extends OperationEvent

/**
 *
 */
@XmlRootElement(name = "operation-create-event")
@XmlType(propOrder = Array("task", "name", "detail"))
case class OperationCreate(

  @xmlElement(required = true) task: TaskCreate,

  @xmlAttribute(required = true) name: String,

  @xmlElement(required = true) detail: OperationDetail)

  extends OperationEvent {

  def this() = this(null, null, null)

}

/**
 *
 */
@XmlRootElement(name = "operation-result-event")
@XmlType(propOrder = Array("result"))
case class OperationResult(

  result: Result[Any])

  extends OperationEvent {

  def this() = this(null)

  @XmlElement(required = true) def getResult = result match {
    case Success(details) if details.isInstanceOf[OperationResultDetail] => details
    case Success(details) => SimpleOperationResultDetail(true, details.toString)
    case Failure(OperationFailed(details)) => details
    case Failure(throwable) => SimpleOperationResultDetail(false, stackTraceToBase64(throwable))
  }

}

/**
 * Transient events to manage the engine components.
 */
case object Redo extends TransientEvent

case object Exists extends TransientEvent

case class Collect(collector: ActorRef) extends TransientEvent

case object Execute extends TransientEvent

/**
 * Transient events for operation handling.
 */

case class OperationExecute(operation: OperationCreate, basedirectory: Path) extends TransientEvent


