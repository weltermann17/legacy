package com.ibm.haploid

package dx

package engine

package domain

package operating

import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlAccessorType }

import akka.actor.{ Props, ActorRef }
import akka.routing.{ SmallestMailboxRouter ⇒ Router }

import com.ibm.haploid.dx.engine.domain.{ MonitorFSM, Monitor }

import core.inject.BindingModule

import domain.{ MonitorFSM, Monitor, Elements, Active }

/**
 *
 */
case class OperatorClass(operatorclass: Class[_ <: OperatorBase], name: String, count: Int, timeout: Long)

/**
 *
 */
case class Operators(operators: Seq[ActorRef])

  extends Elements(operators)

/**
 *
 */
@XmlRootElement(name = "operators")
class OperatorMonitor

  extends Monitor[Operator]

/**
 * Creates, monitors and informs about Operators.
 */
class OperatorMonitorFSM(

  implicit bindingmodule: BindingModule)

  extends MonitorFSM[Operator] {

  startWith(Active, Operators(operatorclasses.foldLeft(Vector[ActorRef]()) {
    case (v, OperatorClass(operatorclass, name, count, timeout)) =>
      v :+ context.actorOf(Props(new OperatorFSM(name, timeout, operatorclass)).withRouter(Router(count)), name = name)
  }))

  initialize

}

