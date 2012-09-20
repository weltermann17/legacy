package com.ibm.haploid

package core

import akka.actor.{ ActorSystem, Cancellable }
import akka.dispatch.{ Future, MessageDispatcher }
import akka.util.duration.longToDurationLong

/**
 * Provide convenient wrappers around akka.dispatch.Future and the like.
 */
package object concurrent {

  import config._

  implicit lazy val actorsystem = ActorSystem.create(getString("haploid.core.concurrent.actorsystem"))

  addShutdownHook(actorsystem.shutdown)

  /**
   * Simply create a [[akka.dispatch.Future]] by providing a body: => T without worrying about an execution context.
   */
  def future[T](body: ⇒ T): Future[T] = {
    require(null != actorsystem, "Internal ActorSystem is null. Did you forget to call concurrent.initialize with a valid ActorSystem?")
    Future(body)(actorsystem.dispatcher)
  }

  /**
   * Spawn a body: => Unit to an execution context and forget about it. Use this only if you have no need to handle errors during the execution of 'body'.
   */
  def spawn(body: ⇒ Any): Unit = {
    require(null != actorsystem, "Internal ActorSystem is null. Did you forget to call concurrent.initialize with a valid ActorSystem?")
    actorsystem.dispatcher.execute(new Runnable { def run = body })
  }

  /**
   * Spawn a body: => Unit to an execution context and forget about it. This versions requires an explicit dispatcher as input.
   */
  def spawn(dispatcher: MessageDispatcher)(body: ⇒ Any): Unit = {
    dispatcher.execute(new Runnable { def run = body })
  }

  /**
   * Schedule 'body' to be executed every 'repeateddelay' milliseconds, but execute it first after 'initialdelay' milliseconds.
   */
  def schedule(initialdelay: Long, repeateddelay: Long)(body: ⇒ Unit) = {
    require(null != actorsystem, "Internal ActorSystem is null. Did you forget to call concurrent.initialize with a valid ActorSystem?")
    actorsystem.scheduler.schedule(initialdelay.milliseconds, repeateddelay.milliseconds)(body)
  }

  /**
   * Schedule 'body' to be executed every 'repeateddelay' milliseconds, but execute it first after 'initialdelay' milliseconds.
   */
  def scheduleOnce(delay: Long)(body: ⇒ Unit): Cancellable = {
    require(null != actorsystem, "Internal ActorSystem is null. Did you forget to call concurrent.initialize with a valid ActorSystem?")
    actorsystem.scheduler.scheduleOnce(delay.milliseconds)(body)
  }

  def addShutdownHook(p: ⇒ Unit) = {
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable { def run = p }))
  }

}
