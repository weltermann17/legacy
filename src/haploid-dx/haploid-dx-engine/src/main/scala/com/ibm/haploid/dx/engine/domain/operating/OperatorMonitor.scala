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
import event.Collect

/**
 *
 */
case class OperatorClass(operatorclass: Class[_ <: OperatorBase], name: String, count: Int, timeout: Long, repeat: Int, repeatTimeout: Long)

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

  val operators = scala.collection.mutable.Map[String, Operator]()

  startWith(Active, Operators(operatorclasses.foldLeft(Vector[ActorRef]()) {
    case (v, OperatorClass(operatorclass, name, count, timeout, repeat, repeatTimeout)) ⇒
      operators.put(name, Operator(name, 0, 0))
      v :+ context.actorOf(Props(new OperatorFSM(name, timeout, repeat, repeatTimeout, operatorclass)).withRouter(Router(count)), name = name)
  }))

  when(Active) {
    case Event(op @ OperatorUpdate(name, succeeded), _) =>
      operators.update(name, operators.get(name) match {
        case Some(old) =>
          if (succeeded)
            Operator(name, old.successes + 1, old.failures)
          else
            Operator(name, old.successes, old.failures + 1)
        case None =>
          Operator(name, 0, 0)
      })

      stay

    case Event(collect @ Collect(collector, _), data) if data.isInstanceOf[Elements] ⇒
      collector ! data

      operators.foreach {
        case (_, value) =>
          collector ! value
      }
      
      stay
  }

  initialize

}

