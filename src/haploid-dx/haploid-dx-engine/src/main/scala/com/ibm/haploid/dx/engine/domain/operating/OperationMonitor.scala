package com.ibm.haploid

package dx

package engine

package domain

package operating

import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlAccessorType }

import akka.actor.{ Props, ActorRef }

import com.ibm.haploid.dx.engine.domain.{ MonitorFSM, Monitor, Elements, Active }

import core.inject.BindingModule

import event.{ TaskCreate, OperationCreate }

/**
 *
 */
case class Operations(operations: Seq[ActorRef])

  extends Elements(operations)

/**
 *
 */
@XmlRootElement(name = "operations")
class OperationMonitor

  extends Monitor[Operation]

/**
 * Creates, monitors and informs about Operations.
 */
case class OperationMonitorFSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends MonitorFSM[Operation] {

  startWith(Active, Operations(Vector.empty))

  when(Active) {
    case Event(create @ OperationCreate(_, name, _), Operations(operations)) ⇒
      val o = context.actorOf(Props(new OperationFSM(create)(bindingmodule)), name = name)
      stay using Operations(operations :+ o)
  }

  initialize

}

