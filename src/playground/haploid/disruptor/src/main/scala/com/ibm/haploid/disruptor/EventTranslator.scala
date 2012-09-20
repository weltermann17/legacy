package com.ibm.haploid.disruptor

trait EventTranslator[E] {
  
  def translateTo(slot: Slot, event: E)

  def translateFrom(slot: Slot): E

}