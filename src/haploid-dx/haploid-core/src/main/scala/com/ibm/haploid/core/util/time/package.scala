package com.ibm.haploid

package core

package util

import core.logger

/**
 * Utilities to ease the handling of time values and simple measurement for profiling.
 */
package object time {

  /**
   * now in milliseconds since 1970
   */
  def now: Long = System.currentTimeMillis

  /**
   * now in nanoseconds since 1970
   */
  def nowNanos: Long = System.nanoTime

  /**
   * Executes f and returns the time elapsed in milliseconds.
   */
  def timeMillis[R](f: => R): (R, Long) = { val begin = now; val r = f; (r, now - begin) }

  /**
   * Executes f and prints the time elapsed in milliseconds.
   */
  def infoMillis[R](f: => R): R = { val r = timeMillis(f); logger.info((r._2 / 1000.0) + " sec"); r._1 }

  /**
   * Executes f and prints message and the time elapsed in nanoseconds.
   */
  def infoMillis[R](msg: String)(f: => R) = { val r = timeMillis(f); logger.info(msg + " " + (r._2 / 1000.0) + " sec"); r._1 }

  /**
   * Executes f and returns the time elapsed in nanoseconds.
   */
  def timeNanos[R](f: => R): (R, Long) = { val begin = nowNanos; val r = f; (r, nowNanos - begin) }

  /**
   * Executes f and prints the time elapsed in nanoseconds.
   */
  def infoNanos[R](f: => R) = { val r = timeNanos(f); logger.info((r._2 / 1000000000.0) + " sec"); r._1 }

  /**
   * Executes f and prints message and the time elapsed in nanoseconds.
   */
  def infoNanos[R](msg: String)(f: => R) = { val r = timeNanos(f); logger.info(msg + " " + (r._2 / 1000000000.0) + " sec"); r._1 }

  import akka.util.duration._

  /**
   * We have replaced our own implementation of time values with [akka.util.Duration], this is simply an alias.
   */
  type Duration = akka.util.Duration

  /**
   * 0 seconds.
   */
  val never = 0 seconds

  /**
   * 100 years, for IT systems this is forever.
   */
  val forever = 100 * 365 days

}
