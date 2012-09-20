package com.ibm.haploid

package dx

package engine

package domain

import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlAccessorType }

import akka.actor.{ Props, ActorRef, InvalidActorNameException }
import akka.dispatch._

import core.inject.BindingModule

import event._
import journal.JournalEntry

/**
 *
 */
case class Jobs(jobs: Seq[ActorRef])

  extends Elements(jobs)

/**
 *
 */
@XmlRootElement(name = "jobs")
class JobMonitor

  extends Monitor[Job]

/**
 * Creates, monitors and informs about Jobs.
 */
class JobMonitorFSM(

  implicit bindingmodule: BindingModule)

  extends MonitorFSM[Job] {

  startWith(Active, Jobs(Vector.empty))

  when(Active) {
    case Event(create @ JobCreate(job, detail), Jobs(jobs)) =>
      val j = context.actorOf(Props(
        job.getConstructors()(0).newInstance(create, bindingmodule).asInstanceOf[JobFSM]),
        name = create.id.toString)
      stay using Jobs(jobs :+ j)
  }

  initialize

}

