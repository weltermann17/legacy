package com.ibm.haploid.disruptor

import java.util.concurrent.atomic.AtomicLongArray
import java.util.concurrent.locks.LockSupport

final class MultiThreadedLowContentionClaimStrategy(
  buffersize: Int) extends ClaimStrategy {

  def getBufferSize = buffersize

  def getSequence = claimsequence.get

  def hasAvailableCapacity(capacity: Int, sequences: Array[Sequence]): Boolean = {
    val wrappoint = (claimsequence.get + capacity) - buffersize
    val m = mingatingsequencethreadlocal.get
    if (wrappoint > m.get) {
      val minsequence = getMinimumSequence(sequences)
      m.set(minsequence)
      if (wrappoint > minsequence) {
        return false
      }
    }
    true
  }

  def incrementAndGet(sequences: Array[Sequence]) = {
    val m = mingatingsequencethreadlocal.get
    waitForCapacity(sequences, m)
    val next = claimsequence.incrementAndGet
    waitForFreeSlotAt(next, sequences, m)
    next
  }

  def incrementAndGet(delta: Long, sequences: Array[Sequence]) = {
    val next = claimsequence.addAndGet(delta)
    waitForFreeSlotAt(next, sequences, mingatingsequencethreadlocal.get)
    next
  }

  def setSequence(sequence: Long, sequences: Array[Sequence]) = {
    claimsequence.lazySet(sequence)
    waitForFreeSlotAt(sequence, sequences, mingatingsequencethreadlocal.get)
  }

  def serializePublishing(sequence: Long, cursor: Sequence, batchsize: Int) = {
    val expected = sequence - batchsize
    while (expected != cursor.get) {
      // busy spin
    }
    cursor.set(sequence)
  }

  private[this] def waitForCapacity(sequences: Array[Sequence], mingatingsequence: MutableLong) = {
    val wrappoint = (claimsequence.get + 1L) - buffersize
    if (wrappoint > mingatingsequence.get) {
      var minsequence = 0L
      while (wrappoint > { minsequence = getMinimumSequence(sequences); minsequence }) {
        LockSupport.parkNanos(1L)
      }
      mingatingsequence.set(minsequence)
    }
  }

  private[this] def waitForFreeSlotAt(sequence: Long, sequences: Array[Sequence], mingatingsequence: MutableLong) = {
    val wrappoint = sequence - buffersize
    if (wrappoint > mingatingsequence.get) {
      var minsequence = 0L
      while (wrappoint > { minsequence = getMinimumSequence(sequences); minsequence }) {
        LockSupport.parkNanos(1L)
      }
      mingatingsequence.set(minsequence)
    }
  }

  private[this] val mingatingsequencethreadlocal = new ThreadLocal[MutableLong] {
    override protected def initialValue = new MutableLong(INITIAL_CURSOR_VALUE)
  }

  private[this] val claimsequence = new PaddedAtomicLong(INITIAL_CURSOR_VALUE)

}
