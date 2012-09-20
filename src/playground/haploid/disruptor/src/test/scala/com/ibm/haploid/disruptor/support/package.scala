package com.ibm.haploid.disruptor

package object support {

  def accumulatedAddition(value: Long): Long = {
    (value * (value - 1L)) >> 1
  }

}