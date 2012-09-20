package com.ibm.haploid

package dx

package engine

package domain

import akka.actor.{ Actor, ActorRef, LoggingFSM }
import akka.dispatch.Await
import akka.pattern.ask

import core.inject.{ Injectable, BindingModule }

import journal.Journal
import marshalling.Marshaled
import event.Exists

/**
 *
 */
abstract class DomainObject

  extends Marshaled

/**
 *
 */
trait DomainObjectState

case object Idle extends DomainObjectState
case object Redoing extends DomainObjectState
case object Active extends DomainObjectState

/**
 *
 */
abstract class DomainObjectFSM[Data](

  implicit val bindingModule: BindingModule)

  extends Injectable

  with Actor

  with LoggingFSM[DomainObjectState, Data] {

  val journal = inject[ActorRef]('journal)

  override def logDepth = 0

  protected[this] def actorFor(path: String): ActorRef = actorFor(path, retriesduringcreate)

  protected[this] def actorFor(path: String, retries: Int): ActorRef = {
    val receiver = context.actorFor(path)
    try {
      Await.result(receiver ? Exists, defaulttimeout.duration)
      receiver
    } catch {
      case _ ⇒
        log.debug("actorFor " + retries + " " + path + " " + receiver)
        Thread.sleep(pauseduringcreate)
        if (0 < retries) {
          actorFor(path, retries - 1)
        } else {
          log.error("actorFor failed " + path + " " + receiver)
          context.system.deadLetters
        }
    }
  }

  protected[this] def ignore: StateFunction = {
    case Event(Exists, _) ⇒ stay replying ()
    case Event((), _) ⇒ stay
  }

  when(Idle)(ignore)

  when(Active)(ignore)

  whenUnhandled(ignore)

}

