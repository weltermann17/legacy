package com.ibm.haploid.disruptor

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.Executor

final class WorkerPool[E](
  ringbuffer: RingBuffer,
  sequencebarrier: SequenceBarrier,
  eventtranslator: EventTranslator[E],
  exceptionhandler: ExceptionHandler[E],
  workhandlers: Array[WorkHandler[E]]) {

  def getWorkerSequences = workprocessors.map(_.getSequence)

  def start(executor: Executor): RingBuffer = {
    if (!started.compareAndSet(false, true)) {
      throw new IllegalStateException(getClass.getSimpleName + " has already been started.")
    }
    val cursor = ringbuffer.getCursor
    worksequence.set(cursor)
    workprocessors.foreach { workprocessor =>
      workprocessor.getSequence.set(cursor)
      executor.execute(workprocessor)
    }
    ringbuffer
  }

  def drainAndHalt = {
    val workersequences = getWorkerSequences
    while (ringbuffer.getCursor > getMinimumSequence(workersequences)) {
      Thread.`yield`
    }
    halt
  }

  def halt = {
    workprocessors.foreach(_.halt)
    started.set(false)
  }

  private[this] val started = new AtomicBoolean(false)

  private[this] val worksequence = new PaddedAtomicLong

  private[this] val numworkers = workhandlers.length

  private[this] val workprocessors = {
    val a = new Array[WorkProcessor[E]](numworkers)
    (0 until numworkers).foreach(i => a.update(i, new WorkProcessor[E](
      ringbuffer,
      sequencebarrier,
      workhandlers(i),
      eventtranslator,
      exceptionhandler,
      worksequence)))
    a
  }

}
