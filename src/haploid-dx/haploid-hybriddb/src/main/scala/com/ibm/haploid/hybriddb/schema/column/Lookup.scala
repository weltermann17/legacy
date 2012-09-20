package com.ibm.haploid

package hybriddb

package schema

package column

import collection.Set

trait Lookup[T]

  extends Sorted[T, Set[Int]] {

  def lookup(value: T): Option[Set[Int]] = representation.get(value)

  def get(value: T) = lookup(value)

}

