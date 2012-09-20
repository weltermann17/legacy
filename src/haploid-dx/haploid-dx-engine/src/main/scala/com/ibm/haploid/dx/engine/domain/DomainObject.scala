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

  extends Marshaled with Serializable

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
  lazy val relauncher = actorFor(journal.path.toString + "/relauncher")

  override def logDepth = 0

  protected[this] def actorFor(path: String): ActorRef = try {
    var retries = retriesduringcreate
    while (-1 < retries) {
      retries -= 1
      val receiver = context.actorFor(path)
      try {
        Await.result(receiver ? Exists, defaulttimeout.duration)
        return receiver
      } catch {
        case e: Throwable ⇒
          Thread.sleep(pauseduringcreate)
      }
    }
    throw new Exception(self.path.toString + " actorFor failed : " + path)
  } catch {
    case e: Throwable ⇒
      log.error(self.path.toString + " actorFor failed : " + path)
      throw new Exception(self.path.toString + " actorFor failed : " + path)
  }

  protected[this] def ignore: StateFunction = {
    case Event(Exists, _) ⇒ stay replying ()
    case Event((), _) ⇒ stay
  }

  when(Idle)(ignore)

  when(Active)(ignore)

  whenUnhandled {
    ignore orElse {
      case event @ Event(_, _) ⇒
        log.error("Unhandled event : self = " + self.path + ", state = " + stateName + ", event = " + event)
        stay
    }
  }

}

