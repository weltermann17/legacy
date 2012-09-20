package com.ibm.haploid.disruptor

trait LifeCycleAware {

  def onStart
  
  def onShutdown
  
}