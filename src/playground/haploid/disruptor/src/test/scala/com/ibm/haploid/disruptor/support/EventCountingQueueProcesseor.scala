package com.ibm.haploid.disruptor.support

import java.util.concurrent.BlockingQueue

import com.ibm.haploid.disruptor._

class EventCountingQueueProcesseor(
  blockingqueue: BlockingQueue[Long],
  counters: Array[PaddedLong],
  index: Int) extends Runnable {

  def halt = running = false
  
  def run = {
    running = true
    while (running) {
      try {
        val event = blockingqueue.take
        counters(index).set(counters(index).get + 1L) 
      } catch {
        case e: InterruptedException => running = false
      }
    }
  }

  @volatile private var running = false

}