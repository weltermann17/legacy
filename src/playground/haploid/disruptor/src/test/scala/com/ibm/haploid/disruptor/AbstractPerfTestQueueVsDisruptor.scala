package com.ibm.haploid.disruptor

import org.junit.Assert.assertTrue
import org.junit.Test

abstract class AbstractPerfTestQueueVsDisruptor {

  protected def testImplementations = {
    val availableprocessors = Runtime.getRuntime.availableProcessors
    if (availableprocessors < getRequiredProcessorsCount) {
      println("Not enough processors available : " + availableprocessors + " < " + getRequiredProcessorsCount)
    }
    val queueops = new Array[Long](RUNS.length)
    val disruptorops = new Array[Long](RUNS.length)
    RUNS.foreach { i =>
      queueops.update(i, runQueuePass)
      println("Completed queue run : " + (i + 1))
    }
    RUNS.foreach { i =>
      disruptorops.update(i, runDisruptorPass)
      println("Completed disruptor run : " + (i + 1))
    }
    printResults(getClass.getSimpleName, disruptorops, queueops)
  }

  def runQueuePass: Long

  def runDisruptorPass: Long

  def getRequiredProcessorsCount: Int

  def shouldCompareDisruptorVsQueue
  
  def printResults(cname: String, disruptorops: Array[Long], queueops: Array[Long]) = {
    RUNS.foreach { i =>
      System.out.format("%s run %d: BlockingQueue=%,d Disruptor=%,d ops/sec\n",
          cname, Integer.valueOf(i + 1), java.lang.Long.valueOf(queueops(i)), java.lang.Long.valueOf(disruptorops(i)))
    }
  }
  
  private val RUNS = 0 to 2

}

