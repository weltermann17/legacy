package com.ibm.haploid.disruptor

final class AggregateEventHandler[E] extends EventHandler[E] with LifeCycleAware {

  def onEvent(event: E, sequence: Long, endofbatch: Boolean) = ()

  def onStart = ()

  def onShutdown = ()

}