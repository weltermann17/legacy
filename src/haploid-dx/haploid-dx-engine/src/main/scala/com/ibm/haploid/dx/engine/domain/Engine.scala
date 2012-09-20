package com.ibm.haploid

package dx

package engine

package domain

import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlAccessorType }

import akka.actor.{ Props, ActorRef }

import core.concurrent.{ actorsystem ⇒ system }
import core.inject.BindingModule

import operating.OperatorMonitorFSM

/**
 *
 */
case class Monitors(

  monitors: Seq[ActorRef])

  extends Elements(monitors)

/**
 *
 */
@XmlRootElement(name = "engine")
class Engine

  extends Monitor[Monitor[_]] {
  
}

/**
 * The core component of the entire DX system.
 */
class EngineFSM private (

  implicit val bindingmodule: BindingModule)

  extends MonitorFSM[Monitor[_]] {

  startWith(Active, Monitors(Vector.empty :+
    context.actorOf(Props(new JobMonitorFSM), name = "jobs") :+
    context.actorOf(Props(new OperatorMonitorFSM), name = "operators")))

  initialize

}

/**
 *
 */
object EngineFSM {

  def apply(implicit bindingmodule: BindingModule) = {
    val engine = system.actorOf(Props(new EngineFSM), name = "engine")
    Thread.sleep(100)
    engine
  }

}

