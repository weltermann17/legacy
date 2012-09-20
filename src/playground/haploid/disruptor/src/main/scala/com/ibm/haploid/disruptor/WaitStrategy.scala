package com.ibm.haploid.disruptor

trait WaitStrategy {

  def signalAllWhenBlocking

  def waitFor(sequence: Long, cursor: Sequence, sequences: Array[Sequence], barrier: SequenceBarrier): Long

  def waitFor(sequence: Long, cursor: Sequence, sequences: Array[Sequence], barrier: SequenceBarrier, timeout: Long, unit: TUnit): Long
  
}
