package com.ibm.haploid

package dx

package engine

package domain

import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlAccessorType }

import akka.actor._
import akka.dispatch._

import core.inject.BindingModule
import event._
import journal.JournalEntry

/**
 *
 */
case class Tasks(tasks: Seq[ActorRef])

  extends Elements(tasks)

/**
 *
 */
@XmlRootElement(name = "tasks")
class TaskMonitor

  extends Monitor[Task]

/**
 * Creates, monitors and informs about Tasks.
 */
class TaskMonitorFSM(

  job: JobCreate)(

    implicit bindingmodule: BindingModule)

  extends MonitorFSM[Task] {

  startWith(Active, Tasks(Vector.empty))

  when(Active) {
    case Event(create @ TaskCreate(task, _, name, _), Tasks(tasks)) =>
      val t = context.actorOf(Props(
        task.getConstructors()(0).newInstance(create, bindingmodule).asInstanceOf[TaskFSM]),
        name = name)
      stay using Tasks(tasks :+ t)
  }

  initialize

}

