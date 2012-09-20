package com.ibm.de.ebs.plm.scala.rest

import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class TaskService(corepoolsize: Int) extends org.restlet.service.TaskService(corepoolsize) {

  private class ThreadFactory extends java.util.concurrent.ThreadFactory {

    override def newThread(runnable: Runnable) = {
      val thread = factory.newThread(runnable)
      threadcount.incrementAndGet
      thread.setName("scala.rest.TaskService." + thread.getName + "/" + threadcount.get)
      /**
       * useful to see who is creating new threads
       *
       * // try { throw new Exception("TaskService: created new thread (" + thread.getName + "), total: " + threadcount.get) } catch { case e => e.printStackTrace }
       *
       */
      thread
    }

    val factory = java.util.concurrent.Executors.defaultThreadFactory
  }

  override protected def createThreadFactory: java.util.concurrent.ThreadFactory = new ThreadFactory

  override protected def createExecutorService(corepoolsize: Int) = {
    val executor = super.createExecutorService(corepoolsize).asInstanceOf[ScheduledThreadPoolExecutor]
    executor.setKeepAliveTime(10, TimeUnit.MINUTES)
    executor.allowCoreThreadTimeOut(true)
    executor
  }

  private val threadcount = new java.util.concurrent.atomic.AtomicInteger
}
