package com.ibm.haploid

package dx

package engine

package journal

import akka.actor.actorRef2Scala
import akka.actor.Actor
import akka.event.Logging

import event.{ Redo, ReceiverEvent }

/**
 *
 */
case class JournalEntry(
  filedescriptor: Long,
  pointer: Long,
  position: Long,
  receiverevent: ReceiverEvent)

/**
 *
 */
trait Journal

  extends Actor {

  def receive = {

    case Redo ⇒ redo; sender ! ()

    case ReceiverEvent(r, e) ⇒ doAppend(ReceiverEvent(r, e)); sender ! ()

  }

  protected[this] def redo

  protected[this] def doAppend(receiverevent: ReceiverEvent)

  protected[this] val log = Logging(context.system, this)

}

