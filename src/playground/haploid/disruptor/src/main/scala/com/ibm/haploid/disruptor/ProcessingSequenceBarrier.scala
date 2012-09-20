package com.ibm.haploid.disruptor

private[disruptor] final class ProcessingSequenceBarrier(
    val waitstrategy: WaitStrategy, 
    val cursor: Sequence, 
    val sequences: Array[Sequence]) extends SequenceBarrier {

  def waitFor(sequence: Long) = {
    checkAlert
    waitstrategy.waitFor(sequence, cursor, sequences, this)
  }
  
  def waitFor(sequence: Long, timeout: Long, unit: TUnit) = {
    checkAlert
    waitstrategy.waitFor(sequence, cursor, sequences, this, timeout, unit)
  }
  
  def getCursor = cursor.get
  
  def isAlerted = alerted
  
  def alert = {
    alerted = true
    waitstrategy.signalAllWhenBlocking
  }
  
  def clearAlert = alerted = false
  
  @volatile private[this] var alerted = false
  
}

