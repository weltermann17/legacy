package com.ibm.haploid

package dx

package engine

package domain

import collection.mutable.HashMap

import akka.actor.{ Actor, ActorRef, ActorPath, PoisonPill }
import akka.event.Logging

import event.Collect

import operating._

/**
 *
 */
class Collector

  extends Actor {

  def receive = {

    case Collect(_) ⇒
      paths.toList.sortWith((x, y) ⇒ x._1.toString.length < y._1.toString.length).headOption match {
        case None ⇒ sender ! ()
        case Some(h) ⇒ sender ! h._2
      }
      self ! PoisonPill

    case Monitors(e) ⇒
      add(sender, new Engine)

    case Jobs(e) ⇒
      add(sender, new JobMonitor)

    case Operators(e) ⇒
      add(sender, new OperatorMonitor)

    case Tasks(e) ⇒
      add(sender, new TaskMonitor)

    case Operations(e) ⇒
      add(sender, new OperationMonitor)

    case element ⇒
      add(sender, element)

  }

  private[this] def add[A](sender: ActorRef, element: A) = {
    log.debug(sender.path.toString)
    paths.put(sender.path, element)
    paths.get(sender.path.parent) match {
      case Some(parent) ⇒
        if (parent.isInstanceOf[Monitor[_]]) parent.asInstanceOf[Monitor[A]].add(element)
        else if (parent.isInstanceOf[Job]) parent.asInstanceOf[Job].add(element.asInstanceOf[TaskMonitor])
        else if (parent.isInstanceOf[Task]) parent.asInstanceOf[Task].add(element.asInstanceOf[OperationMonitor])

      case None ⇒ paths.get(sender.path.parent.parent) match {
        case Some(grandparent) ⇒
          if (grandparent.isInstanceOf[Monitor[_]]) grandparent.asInstanceOf[Monitor[A]].add(element)
        case None ⇒
      }
    }
  }

  private[this] val paths = new HashMap[ActorPath, Any]

  private[this] val log = Logging(context.system, this)

}

