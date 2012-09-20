package com.ibm.haploid.dx.engine.journal

import scala.collection.mutable.HashMap

import akka.actor.actorRef2Scala
import akka.actor.{ ActorPath, Actor }

import com.ibm.haploid.dx.engine.event.{ PersistentEvent, Exists }

case class Subscribers(subscribers: HashMap[ActorPath, Option[JournalEntry]])

case class Relaunch(event: Option[PersistentEvent])

case object RelaunchHerald

class Relauncher

  extends Actor with Publisher {

  def receive = {

    case Exists ⇒
      sender ! ()

    case Subscribers(subscribers) ⇒
      relaunch(subscribers)

  }

  def relaunch(relauncher: HashMap[ActorPath, Option[JournalEntry]]) = relauncher.toList match {

    case sequence: List[(ActorPath, Option[JournalEntry])] ⇒

      sequence.foreach {
        case (receiver, Some(JournalEntry(filedescriptor, pointer, position, receiverevent))) ⇒
          publish(receiver, RelaunchHerald, retriesduringredo)
        case (receiver, None) ⇒
          publish(receiver, RelaunchHerald, retriesduringredo)
      }

      sequence.foreach {
        case (receiver, Some(JournalEntry(filedescriptor, pointer, position, receiverevent))) ⇒
          receiverevent.event.relaunch
          publish(receiver, Relaunch(Some(receiverevent.event)), retriesduringredo)
        case (receiver, None) ⇒
          publish(receiver, Relaunch(None), retriesduringredo)
      }
  }

}
