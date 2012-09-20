package com.ibm.haploid

package hybriddb

package schema

package column

import collection.{ Set, SortedMap }

final class MostlyNullColumn[T] private[column] (

  representation: SortedMap[Option[T], Set[Int]],

  values: Array[Int],

  distinctvalues: Array[Option[T]],

  private[this] val nulls: Set[Int])

  extends CompressedColumn[Option[T]](representation, values, distinctvalues) {
  
  override val length = nulls.size + values.length

  override def get(index: Int): Option[T] = if (nulls.contains(index)) None else super.get(index)

  override def lookup(value: Option[T]): Option[Set[Int]] = if (value.isEmpty) Some(nulls) else super.lookup(value)

  override def iterator = Map(None -> nulls).iterator ++ super.iterator

}

object MostlyNullColumn {

  def apply[T](seq: Seq[Option[T]])(implicit m: Manifest[T], ordering: Ordering[T]) = new MostlyNullColumnBuilder[T].apply(seq)

}

class MostlyNullColumnBuilder[T](

  implicit m: Manifest[T], ordering: Ordering[T])

  extends ColumnBuilder[Option[T], MostlyNullColumn[T]] {

  def newBuilder = new WrapperBuilder[Option[T], Result] {

    def result = wrapped.result match { case (r, v, d, n) => Result(new MostlyNullColumn(r, v, d, n)) }

    val wrapped = new MostlyNullBuilder[T]

  }

}
