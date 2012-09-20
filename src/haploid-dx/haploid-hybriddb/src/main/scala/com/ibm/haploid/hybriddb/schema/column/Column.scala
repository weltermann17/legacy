package com.ibm.haploid

package hybriddb

package schema

package column

trait Column[T] {

  val length: Int

  def get(index: Int): T

  def apply(index: Int): T = if (0 <= index && index < length) get(index) else outofbounds(index)

  protected def outofbounds(index: Int) = throw new IndexOutOfBoundsException(index + " not in [0, " + length + "[")

}

