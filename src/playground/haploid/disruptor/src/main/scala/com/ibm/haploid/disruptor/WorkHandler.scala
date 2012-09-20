package com.ibm.haploid.disruptor

trait WorkHandler[E] {

  def onEvent(event: E)
  
}