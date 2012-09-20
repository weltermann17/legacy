package com.ibm.haploid

package core

import util.text.hexify

/**
 * General purpose utilities: digest, crypt, time, text and more.
 */

package object util {

  def nextAffinity = affinityarray(affinitycounter.incrementAndGet % cores)

  /**
   * more than 63 cores will overflow toLong
   */
  private val cores = scala.math.min(63, Runtime.getRuntime.availableProcessors)

  private val affinityarray = (for (i ← 0 until cores) yield "%X" format scala.math.pow(2, i).toLong).toArray

  private val affinitycounter = new java.util.concurrent.atomic.AtomicInteger

}

