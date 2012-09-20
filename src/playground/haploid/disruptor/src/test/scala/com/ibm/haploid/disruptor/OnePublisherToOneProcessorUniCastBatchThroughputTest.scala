package com.ibm.haploid.disruptor

import java.util.concurrent._
import org.junit._
import org.junit.Assert._
import support._

@Test
final class OnePublisherToOneProcessorUniCastBatchThroughputTest extends AbstractPerfTestQueueVsDisruptor {

  type E = Long

  private[this] val buffersize = 64 * 1024

  private[this] val slotsize = 8

  private[this] val iterations = 100L * 1000L * 1000L

  private[this] val executor = Executors.newSingleThreadExecutor

  private[this] val expectedresult = accumulatedAddition(iterations)

  private[this] val ringbuffer = new RingBuffer(
    slotsize,
    new SingleThreadedClaimStrategy(buffersize),
    new BusySpinWaitStrategy)

  private[this] val sequencebarrier = ringbuffer.newBarrier

  private[this] val eventtranslator = new EventTranslator[E] {

    def translateTo(slot: Slot, event: E) = {
      slot.writeLong(event)
    }

    def translateFrom(slot: Slot): E = {
      slot.readLong
    }

  }

  private[this] val eventhandler = new EventHandler[E] {

    def onEvent(event: E, sequence: Long, endofbatch: Boolean) = {
      value.set(value.get + event)
      if (count == sequence) {
        latch.countDown
      }
    }

    def reset(latch: CountDownLatch, expectedcount: Long) = {
      value.set(0L)
      this.latch = latch
      count = expectedcount
    }

    def getValue = value.get

    private[this] var value = new PaddedAtomicLong
    private[this] var count = 0L
    private[this] var latch: CountDownLatch = null

  }

  private[this] val eventpublisher = new EventPublisher[E](ringbuffer)

  private[this] val batchprocessor = {
    val p1 = new BatchEventProcessor[E](ringbuffer, sequencebarrier, eventtranslator, eventhandler, new IgnoreExceptionHandler[E])
    ringbuffer.setGatingSequences(Array(p1.getSequence))
    p1
  }

  def getRequiredProcessorsCount = 2

  @Test
  def shouldCompareDisruptorVsQueue = testImplementations

  def runQueuePass = 0L

  def runDisruptorPass = {
    val latch = new CountDownLatch(1)
    eventhandler.reset(latch, batchprocessor.getSequence.get + iterations)
    executor.submit(batchprocessor)
    val batchsize = 10
    val start = System.nanoTime
    val batchdescriptor = ringbuffer.newBatchDescriptor(batchsize)
    var offset = 0L
    var i = 0L
    while (i < iterations) {
      ringbuffer.next(batchdescriptor)
      var c = batchdescriptor.getStart
      val end = batchdescriptor.getEnd
      while (c <= end) {
        eventpublisher.translate(eventtranslator, offset, c)
        offset += 1
        c += 1
      }
      ringbuffer.publish(batchdescriptor)
      i += batchsize
    }
    latch.await
    val opspersecond = (iterations * 1000000000L) / (System.nanoTime - start)
    batchprocessor.halt
    assertTrue(expectedresult == eventhandler.getValue)
    opspersecond
  }

}

