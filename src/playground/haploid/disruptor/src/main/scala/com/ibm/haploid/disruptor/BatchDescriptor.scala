package com.ibm.haploid.disruptor

final class BatchDescriptor(size: Int) {

  def getSize = size

  def getEnd = end

  def getStart = end - (size.toLong - 1L)

  private[disruptor] final def setEnd(sequence: Long) = end = sequence

  private[this] final var end = INITIAL_CURSOR_VALUE

}