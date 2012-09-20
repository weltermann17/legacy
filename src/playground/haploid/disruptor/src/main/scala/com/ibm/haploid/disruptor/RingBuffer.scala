package com.ibm.haploid.disruptor

import java.nio.ByteBuffer

final class RingBuffer(
  slotsize: Int,
  claimstrategy: ClaimStrategy,
  waitstrategy: WaitStrategy) extends Sequencer(claimstrategy, waitstrategy) {

  val size = slotsize * claimstrategy.getBufferSize

  require(1 == java.lang.Integer.bitCount(slotsize), "slotsize must be a power of 2")
  require(1 == java.lang.Integer.bitCount(claimstrategy.getBufferSize), "claimstrategy.buffersize must be a power of 2")
  require(0 < size && size <= Integer.MAX_VALUE, "slotsize * buffersize must not be larger than " + Integer.MAX_VALUE + ", but is " + size)

  def this(slotsize: Int, buffersize: Int) = this(
    slotsize,
    new MultiThreadedClaimStrategy(buffersize),
    new BlockingWaitStrategy)

  def getSlot(sequence: Long): Slot = {
    val position = (sequence & indexmask).toInt * slotsize
    new Slot(slots, position, slotsize)
  }
  
  private[this] val indexmask = (claimstrategy.getBufferSize - 1).toLong

  private[this] val slots = ByteBuffer.allocateDirect(size)
  
}

