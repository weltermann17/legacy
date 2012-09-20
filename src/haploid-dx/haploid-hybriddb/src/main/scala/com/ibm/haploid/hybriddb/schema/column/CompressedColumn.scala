package com.ibm.haploid

package hybriddb

package schema

package column

import collection.{ Set, SortedMap }

class CompressedColumn[T] private[column] (

  protected[this] val representation: SortedMap[T, Set[Int]],

  protected[this] val entries: Array[Int],

  protected[this] val distinctvalues: Array[T])

  extends Lookup[T] {

  val length = entries.length

  def get(index: Int): T = distinctvalues(entries(index))

}

object CompressedColumn {

  def apply[T](seq: Seq[T])(implicit m: Manifest[T], ordering: Ordering[T]) = new CompressedColumnBuilder[T].apply(seq)

}

class CompressedColumnBuilder[T](

  implicit m: Manifest[T], ordering: Ordering[T])

  extends ColumnBuilder[T, CompressedColumn[T]] {

  def newBuilder = new WrapperBuilder[T, Result] {

    def result = wrapped.result match { case (r, v, d) => Result(new CompressedColumn(r, v, d)) }

    val wrapped = new CompressedBuilder[T]

  }

}