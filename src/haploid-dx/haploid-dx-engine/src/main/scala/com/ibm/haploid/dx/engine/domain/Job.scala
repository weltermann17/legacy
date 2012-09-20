package com.ibm.haploid

package dx

package engine

package domain

import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlElement, XmlAttribute }

import akka.actor.actorRef2Scala
import akka.actor.Props

import event.{ JobCreate, Collect }
import binding._

/**
 * States of a Job.
 */
sealed trait JobState extends DomainObjectState

case object JobSucceeded extends JobState
case object JobPartiallySucceeded extends JobState
case object JobFailed extends JobState
case object JobArchived extends JobState

/**
 * Details of a Job.
 */
@XmlType(name = "job-detail")
abstract class JobDetail extends DomainObject

/**
 * Data of a Job.
 */
case class JobData(job: JobCreate)

/**
 *
 */
@XmlRootElement(name = "job")
@XmlType(propOrder = Array("id", "status", "created", "detail", "tasks"))
case class Job(

  event: JobCreate,

  @xmlAttribute(required = true) status: String)

  extends DomainObject {

  private def this() = this(null, null)

  @XmlAttribute(required = true) val id = event.id

  @XmlAttribute(required = true) val created = event.created

  @XmlElement(required = true) val detail = event.detail

  @XmlElement def getTasks = tasks

  def add(tm: TaskMonitor) = tasks = tm

  private[this] var tasks: TaskMonitor = null

}

/**
 *
 */
trait JobFSM

  extends DomainObjectFSM[JobData] {

  val job: JobCreate

  startWith(Active, JobData(job))

  whenUnhandled {
    case Event(collect @ Collect(collector), JobData(job)) ⇒
      collector ! Job(job, stateName.toString.toLowerCase)
      tasks ! collect
      stay
  }

  initialize

  protected[this] val tasks = context.actorOf(Props(new TaskMonitorFSM(job)), name = "tasks")

}

