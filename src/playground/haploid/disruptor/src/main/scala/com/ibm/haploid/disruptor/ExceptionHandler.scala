package com.ibm.haploid.disruptor

trait ExceptionHandler[E] {

  def handleEventException(ex: Throwable, sequence: Long, event: E)

  def handleOnStartException(ex: Throwable)

  def handleOnShutdownException(ex: Throwable)

}

sealed class IgnoreExceptionHandler[E] extends ExceptionHandler[E] {

  def handleEventException(ex: Throwable, sequence: Long, event: E) = {
    ex.printStackTrace
    println("Exception processing : " + sequence + " " + event)
    println(ex)
    throw new RuntimeException(ex)
  }

  def handleOnStartException(ex: Throwable) = {
    println("Exception during onStart : " + ex)
  }

  def handleOnShutdownException(ex: Throwable) = {
    println("Exception during onShutdown : " + ex)
  }

}

final class FatalExceptionHandler[E] extends IgnoreExceptionHandler[E] {

  override def handleEventException(ex: Throwable, sequence: Long, event: E) = {
    super.handleEventException(ex, sequence, event)
    throw new RuntimeException(ex)
  }

}