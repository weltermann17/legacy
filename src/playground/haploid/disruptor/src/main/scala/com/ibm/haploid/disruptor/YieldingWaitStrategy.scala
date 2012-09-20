package com.ibm.haploid.disruptor

import java.util.concurrent.locks.LockSupport

final class YieldingWaitStrategy extends WaitStrategy {

  def signalAllWhenBlocking = ()

  def waitFor(sequence: Long, cursor: Sequence, sequences: Array[Sequence], barrier: SequenceBarrier) = {
    var availablesequence = INITIAL_CURSOR_VALUE
    var counter = retries
    if (0 == sequences.length) {
      while ({ availablesequence = cursor.get; availablesequence } < sequence) {
        counter = applyWaitMethod(barrier, counter)
      }
    } else {
      while ({ availablesequence = getMinimumSequence(sequences); availablesequence } < sequence) {
        counter = applyWaitMethod(barrier, counter)
      }
    }
    availablesequence
  }

  def waitFor(sequence: Long, cursor: Sequence, sequences: Array[Sequence], barrier: SequenceBarrier, timeout: Long, unit: TUnit) = {
    val timeoutms = unit.toMillis(timeout)
    val start = System.currentTimeMillis
    var availablesequence = INITIAL_CURSOR_VALUE
    var counter = retries
    var done = false
    @inline def body = {
      counter = applyWaitMethod(barrier, counter)
      val elapsedms = System.currentTimeMillis - start
      if (elapsedms > timeoutms) done = true
    }
    if (0 == sequences.length) {
      while (!done && { availablesequence = cursor.get; availablesequence } < sequence) {
        body
      }
    } else {
      while (!done && { availablesequence = getMinimumSequence(sequences); availablesequence } < sequence) {
        body
      }
    }
    availablesequence
  }

  @inline private[this] def applyWaitMethod(barrier: SequenceBarrier, c: Int) = {
    var counter = c
    barrier.checkAlert
    if (0 == counter) {
      Thread.`yield`
    } else {
      counter -= 1
    }
    counter
  }

  private[this] val retries = 100

}
