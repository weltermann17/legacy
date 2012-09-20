package com.ibm.haploid

package dx

package engine

package domain

import javax.xml.bind.annotation.XmlRootElement

import akka.actor.{ SupervisorStrategy, Props, OneForOneStrategy, ActorRef }

import core.inject.BindingModule
import core.util.text.stackTraceToString

import event.{ TaskCreate, JobCreate }

/**
 *
 */
case class TaskClass(taskclass: Class[_ <: TaskFSM], name: String)

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

  override val supervisorStrategy = OneForOneStrategy() {
    case _: Throwable ⇒ SupervisorStrategy.Stop
  }

  startWith(Active, Tasks(Vector.empty))

  when(Active) {
    case Event(create @ TaskCreate(_, name, detail), Tasks(tasks)) ⇒
      try {
        val t = context.actorOf(Props(
          taskclasses.get(detail.name).get.taskclass.getConstructors()(0).newInstance(create, bindingmodule).asInstanceOf[BaseTaskFSM[_]]),
          name = name)
        stay using Tasks(tasks :+ t)
      } catch {
        case e: Throwable ⇒
          log.error(stackTraceToString(e))
          throw e
      }
  }

  initialize

}

