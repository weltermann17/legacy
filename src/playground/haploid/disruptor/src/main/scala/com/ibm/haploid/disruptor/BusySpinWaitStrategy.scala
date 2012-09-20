package com.ibm.haploid.disruptor

final class BusySpinWaitStrategy extends WaitStrategy {

  def signalAllWhenBlocking = ()
  
  def waitFor(sequence: Long, cursor: Sequence, sequences: Array[Sequence], barrier: SequenceBarrier) = {
    var availablesequence = INITIAL_CURSOR_VALUE
    if (0 == sequences.length) {
      while ({ availablesequence = cursor.get; availablesequence } < sequence) {
        barrier.checkAlert
      }
    } else {
      while ({ availablesequence = getMinimumSequence(sequences); availablesequence } < sequence) {
        barrier.checkAlert
      }
    }
    availablesequence
  }
  
  def waitFor(sequence: Long, cursor: Sequence, sequences: Array[Sequence], barrier: SequenceBarrier, timeout: Long, unit: TUnit) = {
    val timeoutms = unit.toMillis(timeout)
    val start = System.currentTimeMillis
    var availablesequence = INITIAL_CURSOR_VALUE
    var done = false
    @inline def body = {
      barrier.checkAlert
      val elapsedms = System.currentTimeMillis - start
      if (elapsedms > timeoutms) {
        done = true
      }
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
  
}
