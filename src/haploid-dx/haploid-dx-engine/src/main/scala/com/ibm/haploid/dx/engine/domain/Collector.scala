package com.ibm.haploid

package dx

package engine

package domain

import collection.mutable.HashMap
import akka.actor.{ Actor, ActorRef, ActorPath, PoisonPill }
import akka.event.Logging
import event.Collect
import operating._

//trait Collectable {
//
//}

case class CollectResult(element: Any, childsCollected: Int, childs: Option[Int])

/**
 *
 */
class Collector

  extends Actor {

  def receive = {

    case Collect(actorRef, depth) ⇒
      this.owner = sender
      actorRef ! Collect(self, depth)

    case CollectResult(Monitors(_), childs, _) if childs > 0 ⇒
      add(sender, new Engine, Some(childs))

    case CollectResult(Monitors(_), 0, Some(childs)) ⇒
      add(sender, new Engine withSize childs)

    case CollectResult(Jobs(_), childs, _) if childs > 0 ⇒
      add(sender, new JobMonitor, Some(childs))

    case CollectResult(Jobs(_), 0, Some(childs)) ⇒
      add(sender, new JobMonitor withSize childs)

    case CollectResult(Tasks(_), childs, _) if childs > 0 ⇒
      add(sender, new TaskMonitor, Some(childs))

    case CollectResult(Tasks(_), 0, Some(childs)) ⇒
      add(sender, new TaskMonitor withSize childs)

    case CollectResult(Operations(_), childs, _) if childs > 0 ⇒
      add(sender, new OperationMonitor, Some(childs))

    case CollectResult(Operations(_), 0, Some(childs)) ⇒
      add(sender, new OperationMonitor withSize childs)

    case CollectResult(el @ (Job(_, _, _) | Task(_, _, _, _, _)), childs, _) ⇒
      add(sender, el, Some(childs))

    case Operators(e) ⇒
      add(sender, new OperatorMonitor, Some(e.size))

    case element ⇒
      add(sender, element)

  }

  private[this] def done = {
    paths.find(_._2._2 > 0) match {
      case Some(_) ⇒
      case None ⇒
        paths.toList.sortWith((x, y) ⇒ x._1.toString.length < y._1.toString.length).headOption match {
          case None ⇒ owner ! ()
          case Some(h) ⇒ owner ! h._2._1
        }

        self ! PoisonPill
    }
  }

  private[this] def add[A](sender: ActorRef, element: A, childs: Option[Int] = None) = {
    childs match {
      case None ⇒
        if (!element.isInstanceOf[Operator])
          paths.put(sender.path, (element, 0))
      case Some(i) ⇒
        paths.put(sender.path, (element, i))
    }

    val parentPath = if (element.isInstanceOf[Operator]) {
      sender.path
    } else {
      sender.path.parent
    }

    paths.get(parentPath) match {
      case Some((parent, i)) ⇒
        var dec = true

        if (parent.isInstanceOf[Monitor[_]]) parent.asInstanceOf[Monitor[A]].add(element)
        else if (parent.isInstanceOf[Job]) parent.asInstanceOf[Job].add(element.asInstanceOf[TaskMonitor])
        else if (parent.isInstanceOf[Task]) parent.asInstanceOf[Task].add(element.asInstanceOf[Monitor[A]])
        else dec = false

        if (dec) paths.put(parentPath, (parent, i - 1))

      case None ⇒
    }

    done
  }

  private[this] var owner = self

  private[this] val paths = new HashMap[ActorPath, (Any, Int)]

  private[this] val log = Logging(context.system, this)

}