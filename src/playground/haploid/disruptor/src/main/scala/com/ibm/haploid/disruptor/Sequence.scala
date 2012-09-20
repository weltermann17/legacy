package com.ibm.haploid.disruptor

import java.util.concurrent.atomic.AtomicLongFieldUpdater

final class Sequence(initialvalue: Long) {
  
  private[this] val updater = {
    if (null == Sequence.updater) {
      Sequence.updater = AtomicLongFieldUpdater.newUpdater(classOf[Sequence], "value")
    }
    Sequence.updater
  }

  value = initialvalue

  def this() = this(INITIAL_CURSOR_VALUE)

  def set(v: Long) = updater.lazySet(this, v)

  def get = value

  def compareAndSet(expected: Long, next: Long): Boolean = updater.compareAndSet(this, expected, next)

  override def toString = value.toString

  def sumPadding = p1 + p2 + p3 + p4 + p5 + p6 + p7 + value + q1 + q2 + q3 + q4 + q5 + q6 + q7

  def setPadding(v: Long): Unit = {
    p1 = v
    p2 = v
    p3 = v
    p4 = v
    p5 = v
    p6 = v
    p7 = v
    q1 = v
    q2 = v
    q3 = v
    q4 = v
    q5 = v
    q6 = v
    q7 = v
  }

  @volatile private[this] var p1 = 7L
  @volatile private[this] var p2 = 7L
  @volatile private[this] var p3 = 7L
  @volatile private[this] var p4 = 7L
  @volatile private[this] var p5 = 7L
  @volatile private[this] var p6 = 7L
  @volatile private[this] var p7 = 7L
  @volatile private[this]var value = initialvalue
  @volatile private[this] var q1 = 7L
  @volatile private[this] var q2 = 7L
  @volatile private[this] var q3 = 7L
  @volatile private[this] var q4 = 7L
  @volatile private[this] var q5 = 7L
  @volatile private[this] var q6 = 7L
  @volatile private[this] var q7 = 7L

}

object Sequence {

  private var updater: AtomicLongFieldUpdater[Sequence] = null

}

