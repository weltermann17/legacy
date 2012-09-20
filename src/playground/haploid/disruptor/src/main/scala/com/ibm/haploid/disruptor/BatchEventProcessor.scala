package com.ibm.haploid.disruptor

import java.util.concurrent.atomic.AtomicBoolean

final class BatchEventProcessor[E](
  ringbuffer: RingBuffer,
  sequencebarrier: SequenceBarrier,
  eventtranslator: EventTranslator[E],
  eventhandler: EventHandler[E],
  exceptionhandler: ExceptionHandler[E]) extends EventProcessor {

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
    var nextsequence = sequence.get + 1L
    var done = false
    while (!done) {
      try {
        val availablesequence = sequencebarrier.waitFor(nextsequence)
        while (nextsequence <= availablesequence) {
          val event = eventtranslator.translateFrom(ringbuffer.getSlot(nextsequence))
          error = event
          eventhandler.onEvent(event, nextsequence, nextsequence == availablesequence)
          nextsequence += 1L
        }
        sequence.set(nextsequence - 1L)
      } catch {
        case e: AlertException.type =>
          if (!running.get) done = true
        case e =>
          exceptionhandler.handleEventException(e, nextsequence, error.asInstanceOf[E])
          sequence.set(nextsequence)
          nextsequence += 1L
      }
    }
    notifyShutdown
    running.set(false)
  }

  @inline private[this] final def notifyStart = {
    if (eventhandler.isInstanceOf[LifeCycleAware]) {
      try {
        eventhandler.asInstanceOf[LifeCycleAware].onStart
      } catch {
        case e => exceptionhandler.handleOnStartException(e)
      }
    }
  }

  @inline private[this] final def notifyShutdown = {
    if (eventhandler.isInstanceOf[LifeCycleAware]) {
      try {
        eventhandler.asInstanceOf[LifeCycleAware].onShutdown
      } catch {
        case e => exceptionhandler.handleOnShutdownException(e)
      }
    }
  }

  private[this] final val sequence = new Sequence(INITIAL_CURSOR_VALUE)

  private[this] final val running = new AtomicBoolean(false)

  if (eventhandler.isInstanceOf[SequenceReportingEventHandler[_]]) {
    eventhandler.asInstanceOf[SequenceReportingEventHandler[_]].setSequenceCallback(sequence)
  }

}