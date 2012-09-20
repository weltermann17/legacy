package com.ibm.haploid

package hybriddb

package schema

package column

trait Unique[T]

  extends Sorted[T, Int] {

  def unique(value: T): Option[Int] = representation.get(value)

  def get(value: T) = unique(value)

}

