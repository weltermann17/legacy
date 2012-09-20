package com.ibm.haploid.disruptor

trait ClaimStrategy {

  def getBufferSize: Int

  def hasAvailableCapacity(capacity: Int, sequences: Array[Sequence]): Boolean
  
  def incrementAndGet(sequences: Array[Sequence]): Long
  
  def incrementAndGet(delta: Long, sequences: Array[Sequence]): Long
  
  def setSequence(sequence: Long, seauences: Array[Sequence])
  
  def serializePublishing(sequence: Long, cursor: Sequence, batchsize: Int)
  
}