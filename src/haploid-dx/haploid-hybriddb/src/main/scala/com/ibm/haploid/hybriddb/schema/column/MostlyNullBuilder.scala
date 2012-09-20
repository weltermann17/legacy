package com.ibm.haploid

package hybriddb

package schema

package column

import scala.collection.{ BitSet, Set, SortedMap }
import scala.collection.mutable.{ BitSet => MutableBitSet }
import scala.collection.mutable.Builder

final class MostlyNullBuilder[T](

  implicit m: Manifest[T], ordering: Ordering[T])

  extends Builder[Option[T], (SortedMap[Option[T], Set[Int]], Array[Int], Array[Option[T]], BitSet)] {

  def +=(elem: Option[T]) = {
    if (elem.isEmpty) nulls += i else compressedbuilder += elem
    i += 1
    this
  }

  def result = {
    val r = compressedbuilder.result
    (r._1, r._2, r._3, nulls)
  }

  def clear = { compressedbuilder.clear; nulls.clear; i = 0 }

  private[this] var i = 0

  private[this] val compressedbuilder = new CompressedBuilder[Option[T]]

  private[this] val nulls = new MutableBitSet

}

