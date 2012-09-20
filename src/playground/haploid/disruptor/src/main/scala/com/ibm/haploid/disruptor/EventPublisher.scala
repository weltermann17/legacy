package com.ibm.haploid.disruptor

class EventPublisher[E](ringbuffer: RingBuffer) {

  def publishEvent(translator: EventTranslator[E], event: E) = {
    val sequence = ringbuffer.next
    translateAndPublish(translator, event, sequence)
  }

  def publishEvent(translator: EventTranslator[E], event: E, timeout: Long, unit: TUnit) = {
    val sequence = ringbuffer.next(timeout, unit)
    translateAndPublish(translator, event, sequence)
  }

  def translate(translator: EventTranslator[E], event: E, sequence: Long) = {
    translator.translateTo(ringbuffer.getSlot(sequence), event)
  }

  @inline private[this] final def translateAndPublish(translator: EventTranslator[E], event: E, sequence: Long) = {
    try {
      translator.translateTo(ringbuffer.getSlot(sequence), event)
    } finally {
      ringbuffer.publish(sequence)
    }
  }

}
