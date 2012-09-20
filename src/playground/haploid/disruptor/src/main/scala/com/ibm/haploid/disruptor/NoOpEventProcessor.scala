package com.ibm.haploid.disruptor

final class NoOpEventProcessor[E](sequencer: Sequencer) extends EventProcessor {

  def getSequence: Sequence = new Sequence(sequencer.getCursor)
  
  def halt = ()
  
  def run = ()
  
}