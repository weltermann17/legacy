package com.ibm.haploid.disruptor

import org.junit._
import org.junit.Assert._
import org.hamcrest.core.Is._

@Test
final class BatchPublisherTest {

  private val ringbuffer = new RingBuffer(
      512,
      new MultiThreadedClaimStrategy(32),
      new BlockingWaitStrategy)
  
  private val sequencebarrier = ringbuffer.newBarrier(new Array[Sequence](0))
  
  ringbuffer.setGatingSequences(Array(new NoOpEventProcessor[Long](ringbuffer).getSequence))
  
  @Test
  def shouldClaimBatchAndPublishBack = {
    val batchsize = 5
    val batchdescriptor = ringbuffer.newBatchDescriptor(batchsize)
    ringbuffer.next(batchdescriptor)
    assertThat(java.lang.Long.valueOf(batchdescriptor.getStart), is(java.lang.Long.valueOf(0L)))
    assertThat(java.lang.Long.valueOf(batchdescriptor.getEnd), is(java.lang.Long.valueOf(4L)))
    assertThat(java.lang.Long.valueOf(ringbuffer.getCursor), is(java.lang.Long.valueOf(INITIAL_CURSOR_VALUE)))
    ringbuffer.publish(batchdescriptor)
    assertThat(java.lang.Long.valueOf(ringbuffer.getCursor), is(java.lang.Long.valueOf(batchsize - 1L)))
    assertThat(java.lang.Long.valueOf(sequencebarrier.waitFor(0L)), is(java.lang.Long.valueOf(batchsize - 1L)))
   }

}