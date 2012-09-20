package com.ibm.de.ebs.plm.scala.concurrent

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.Future
import java.util.concurrent.Callable
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object ops {

  // if two threads try to execute p simultaneously only one will succeed, the other one will simply skip p
  // there can only be one exclusive block in a class extending Exclusive

  trait Exclusive {
    def exclusive(p: => Unit) = if (acquire) scala.concurrent.ops.spawn { p; release }
    private def acquire = !lock.getAndSet(true)
    private def release = lock.set(false)
    private val lock = new AtomicBoolean
  }

  // if two+ threads try to execute p simultaneously only one will succeed, the others block until p has finished
  // any further calls to p will simply skip p
  // there can only be one onlyonce block in a class extending OnlyOnce

  trait OnlyOnce {
    def onlyonce(p: => Unit) = {
      if (!done.get) {
        try {
          doing.lock
          if (!done.get) {
            p;
            done.set(true)
          }
        } finally {
          doing.unlock
        }
      }
    }
    private val done = new AtomicBoolean
    private val doing = new ReentrantLock
  }

  def spawn(p: => Unit)(implicit scheduler: ScheduledExecutorService) = {
    scheduler.execute(new Runnable { def run = p })
  }

  def schedule(delay: Long)(p: => Unit)(implicit scheduler: ScheduledExecutorService) = {
    scheduler.scheduleWithFixedDelay(
      new Runnable { def run = p },
      delay,
      delay,
      TimeUnit.MILLISECONDS)
  }

  def schedule(initialdelay: Long, repeateddelay: Long)(p: => Unit)(implicit scheduler: ScheduledExecutorService) = {
    scheduler.scheduleWithFixedDelay(
      new Runnable { def run = p },
      initialdelay,
      repeateddelay,
      TimeUnit.MILLISECONDS)
  }

  def future[T](p: => T)(implicit scheduler: ScheduledExecutorService): Future[T] = {
    scheduler.submit(new Callable[T] { def call: T = p })
  }

  case class RichFuture[T](future: Future[T]) {
    def get(timeout: Long) = future.get(timeout, TimeUnit.MILLISECONDS)
  }

  implicit def future2richfuture[T](f: Future[T]): RichFuture[T] = new RichFuture[T](f)

}

