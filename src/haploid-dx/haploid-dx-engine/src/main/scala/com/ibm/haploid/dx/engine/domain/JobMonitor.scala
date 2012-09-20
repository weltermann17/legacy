package com.ibm.haploid

package dx

package engine

package domain

import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlAccessorType }

import akka.actor.{ Props, ActorRef, InvalidActorNameException, OneForOneStrategy, SupervisorStrategy }
import akka.dispatch._
import core.inject.BindingModule

import event._
import journal.JournalEntry

/**
 *
 */
case class JobClass(jobclass: Class[_ <: JobFSM], name: String, continuous: Boolean)

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

  override def preStart = {
    super.preStart()
    val continiuousJobs = jobclasses.filter(_._2.continuous)
    continiuousJobs.foreach {
      case (jobname, job) ⇒
        self ! new JobCreate(ContinuousJobDetail(jobname)) {
          override val id = jobname
        }
    }
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case _: Throwable ⇒ SupervisorStrategy.Stop
  }

  startWith(Active, Jobs(Vector.empty))

  when(Active) {
    case Event(create @ JobCreate(detail, trigger), Jobs(jobs)) ⇒
      try {
        create._counter = jobs.size
        val job = context.actorOf(Props(
          jobclasses.get(detail.name).get.jobclass.getConstructors()(0).newInstance(create, bindingmodule).asInstanceOf[JobFSM]),
          name = create.id)
        stay using Jobs(jobs :+ job)
      } catch {
        case e: Throwable ⇒
          log.warning("JobMonitor : " + e)
          stay
      }
  }

  initialize

}

