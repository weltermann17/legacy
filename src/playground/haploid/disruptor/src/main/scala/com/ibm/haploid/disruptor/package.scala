package com.ibm.haploid

import java.util.concurrent.atomic.AtomicLong
import com.ibm.haploid.io.BinaryFormatByteBuffer

package object disruptor {

  type TUnit = java.util.concurrent.TimeUnit

  type Slot = BinaryFormatByteBuffer

  final val INITIAL_CURSOR_VALUE = -1L

  final def getMinimumSequence(sequences: Array[Sequence]): Long = {
    var m = Long.MaxValue
    sequences.foreach(s => m = math.min(m, s.get))
    m
  }

  sealed class MutableLong(v: Long) {

    value = v

    def this() = this(INITIAL_CURSOR_VALUE)

    def get = value

    def set(v: Long) = value = v

    private[this] var value = 0L

  }

  final class PaddedLong(v: Long) extends MutableLong(v) {

    def this() = this(INITIAL_CURSOR_VALUE)

    def sumPadding = p1 + p2 + p3 + p4 + p5 + p6

    def setPadding(v: Long) = {
      p1 = v
      p2 = v
      p3 = v
      p4 = v
      p5 = v
      p6 = v
    }

    @volatile private[this] var p1 = 7L
    @volatile private[this] var p2 = 7L
    @volatile private[this] var p3 = 7L
    @volatile private[this] var p4 = 7L
    @volatile private[this] var p5 = 7L
    @volatile private[this] var p6 = 7L

  }

  final class PaddedAtomicLong(v: Long) extends AtomicLong(v) {

    def this() = this(INITIAL_CURSOR_VALUE)

    def sumPadding = p1 + p2 + p3 + p4 + p5 + p6

    def setPadding(v: Long) = {
      p1 = v
      p2 = v
      p3 = v
      p4 = v
      p5 = v
      p6 = v
    }

    @volatile private[this] var p1 = 7L
    @volatile private[this] var p2 = 7L
    @volatile private[this] var p3 = 7L
    @volatile private[this] var p4 = 7L
    @volatile private[this] var p5 = 7L
    @volatile private[this] var p6 = 7L

  }

}