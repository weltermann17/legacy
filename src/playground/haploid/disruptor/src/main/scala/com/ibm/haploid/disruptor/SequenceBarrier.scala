package com.ibm.haploid.disruptor

trait SequenceBarrier {

  def waitFor(sequence: Long): Long
  
  def waitFor(sequence: Long, timeout: Long, unit: TUnit): Long
  
  def getCursor: Long
  
  def isAlerted: Boolean
  
  def alert
  
  def clearAlert 
  
  def checkAlert = if (isAlerted) throw AlertException
  
}