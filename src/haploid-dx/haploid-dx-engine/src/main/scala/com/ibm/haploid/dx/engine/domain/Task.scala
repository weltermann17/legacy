package com.ibm.haploid

package dx

package engine

package domain

import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlElement, XmlAttribute }

import akka.actor.actorRef2Scala
import akka.actor.Props

import event.{ TaskCreate, Collect }
import operating.{ OperationMonitorFSM, OperationMonitor }
import binding._

/**
 * States of a Task.
 */
sealed trait TaskState extends DomainObjectState

case object TaskSucceeded extends TaskState
case object TaskFailed extends TaskState
case object TaskArchived extends TaskState

/**
 * Details of a Task.
 */
abstract class TaskDetail extends DomainObject

/**
 * Data of a Task.
 */
case class TaskData(task: TaskCreate)

/**
 *
 */
@XmlRootElement(name = "task")
@XmlType(propOrder = Array("id", "job", "name", "status", "created", "detail", "operations"))
case class Task(

  event: TaskCreate,

  @xmlAttribute(required = true) name: String,

  @xmlAttribute(required = true) status: String)

  extends DomainObject {

  private def this() = this(null, null, null)

  @XmlAttribute(required = true) val id = event.id

  @XmlAttribute(required = true) val job = event.job.id

  @XmlAttribute(required = true) val created = event.created

  @XmlElement(required = true) val detail = event.detail

  @XmlElement def getOperations = operations

  def add(om: OperationMonitor) = operations = om

  private[this] var operations: OperationMonitor = null

}

/**
 *
 */
trait TaskFSM

  extends DomainObjectFSM[TaskData] {

  val task: TaskCreate

  protected[this] val operations = context.actorOf(Props(new OperationMonitorFSM(task)), name = "operations")

  startWith(Idle, TaskData(task))

  whenUnhandled {
    case Event(collect @ Collect(collector), TaskData(task)) ⇒
      collector ! Task(task, self.path.name, stateName.toString.toLowerCase)
      operations ! collect
      stay
  }

  initialize

}


