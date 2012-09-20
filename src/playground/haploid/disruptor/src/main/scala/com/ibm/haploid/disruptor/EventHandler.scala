package com.ibm.haploid.disruptor

trait EventHandler[E] {

  def onEvent(event: E, sequence: Long, endofbatch: Boolean)
  
}

trait SequenceReportingEventHandler[E] extends EventHandler[E] {
  
  def setSequenceCallback(sequencecallback: Sequence)
  
}