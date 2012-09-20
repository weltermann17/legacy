package com.ibm.haploid.disruptor

import java.util.concurrent.locks.LockSupport

final class SingleThreadedClaimStrategy(
  buffersize: Int) extends ClaimStrategy {

  def getBufferSize = buffersize

  def getSequence = claimsequence.get

  def hasAvailableCapacity(capacity: Int, sequences: Array[Sequence]): Boolean = {
    val wrappoint = (claimsequence.get + capacity) - buffersize
    if (wrappoint > mingatingsequence.get) {
      val minsequence = getMinimumSequence(sequences)
      mingatingsequence.set(minsequence)
      if (wrappoint > minsequence) {
        return false
      }
    }
    true
  }

  def incrementAndGet(sequences: Array[Sequence]) = {
    val nextsequence = claimsequence.get + 1L
    claimsequence.set(nextsequence)
    waitForFreeSlotAt(nextsequence, sequences)
    nextsequence
  }

  def incrementAndGet(delta: Long, sequences: Array[Sequence]) = {
    val nextsequence = claimsequence.get + delta
    claimsequence.set(nextsequence)
    waitForFreeSlotAt(nextsequence, sequences)
    nextsequence
  }

  def setSequence(sequence: Long, sequences: Array[Sequence]) = {
    claimsequence.set(sequence)
    waitForFreeSlotAt(sequence, sequences)
  }

  def serializePublishing(sequence: Long, cursor: Sequence, batchsize: Int) = {
    cursor.set(sequence)
  }

  @inline private[this] def waitForFreeSlotAt(sequence: Long, sequences: Array[Sequence]) = {
    val wrappoint = sequence - buffersize
    if (wrappoint > mingatingsequence.get) {
      var minsequence = INITIAL_CURSOR_VALUE
      while (wrappoint > { minsequence = getMinimumSequence(sequences); minsequence }) {
        LockSupport.parkNanos(1L)
      }
      mingatingsequence.set(minsequence)
    }
  }

  private[this] val mingatingsequence = new PaddedLong

  private[this] val claimsequence = new PaddedLong

}
