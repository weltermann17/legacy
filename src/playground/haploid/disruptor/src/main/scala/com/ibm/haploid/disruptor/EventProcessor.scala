package com.ibm.haploid.disruptor

trait EventProcessor extends Runnable {

  def getSequence: Sequence
  
  def halt
  
}