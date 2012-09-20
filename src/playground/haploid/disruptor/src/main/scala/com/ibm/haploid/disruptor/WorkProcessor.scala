package com.ibm.haploid.disruptor

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

final class WorkProcessor[E](
  ringbuffer: RingBuffer,
  sequencebarrier: SequenceBarrier,
  workhandler: WorkHandler[E],
  eventtranslator: EventTranslator[E],
  exceptionhandler: ExceptionHandler[E],
  workingsequence: AtomicLong) extends EventProcessor {

  def getSequence = sequence

  def halt = {
    running.set(false)
    sequencebarrier.alert
  }

  def run = {
    if (!running.compareAndSet(false, true)) {
      throw new IllegalStateException(getClass.getSimpleName + " already running.")
    }
    sequencebarrier.clearAlert
    notifyStart
    var error: Any = null
    var nextsequence = sequence.get
    var done = false
    var processedsequence = true
    while (!done) {
      try {
        if (processedsequence) {
          processedsequence = false
          nextsequence = workingsequence.incrementAndGet
          sequence.set(nextsequence - 1L)
        }
        sequencebarrier.waitFor(nextsequence)
        val event = eventtranslator.translateFrom(ringbuffer.getSlot(nextsequence))
        error = event
        workhandler.onEvent(event)
        processedsequence = true
      } catch {
        case e: AlertException.type =>
          if (!running.get) done = true
        case e =>
          exceptionhandler.handleEventException(e, nextsequence, error.asInstanceOf[E])
          processedsequence = true
      }
    }
    notifyShutdown
    running.set(false)
  }

  @inline private def notifyStart = {
    try {
      workhandler.asInstanceOf[LifeCycleAware].onStart
    } catch {
      case e => exceptionhandler.handleOnStartException(e)
    }
  }

  @inline private def notifyShutdown = {
    if (workhandler.isInstanceOf[LifeCycleAware]) {
      try {
        workhandler.asInstanceOf[LifeCycleAware].onShutdown
      } catch {
        case e => exceptionhandler.handleOnShutdownException(e)
      }
    }
  }

  private[this] val sequence = new Sequence(INITIAL_CURSOR_VALUE)

  private[this] val running = new AtomicBoolean(false)

}