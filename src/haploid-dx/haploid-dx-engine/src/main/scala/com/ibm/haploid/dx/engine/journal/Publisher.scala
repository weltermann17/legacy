package com.ibm.haploid.dx

package engine

package journal

import akka.actor.actorRef2Scala
import akka.actor.{ActorPath, Actor}
import akka.dispatch.Await
import akka.event.Logging
import akka.pattern.ask

import com.ibm.haploid.dx.engine.event.Exists

private[journal] trait Publisher extends Actor {

  private[this] val log = Logging(context.system, this)

  def publish(receiverPath: ActorPath, event: Any, maxretries: Int, onFailure: Option[Int ⇒ Unit] = None): Unit = try {
    var retries = maxretries
    while (-1 < retries) {
      retries -= 1
      val receiver = context.system.actorFor(receiverPath)
      try {
        if (receiver != self) {
          Await.result(receiver ? Exists, defaulttimeout.duration)
          receiver ! event
        }

        return ()
      } catch {
        case e: Throwable ⇒
          Thread.sleep(pauseduringredo)
          if (onFailure.isDefined) onFailure.get(retries)
      }
    }
  } catch {
    case e: Throwable ⇒
      log.error("publish failed : " + e)
  }

}