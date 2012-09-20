package com.ibm.haploid

package hybriddb

package schema

package column

import collection.{ Set, SortedMap }
import collection.mutable.Builder

class BitSetColumn[T] private[column] (

  protected[this] val representation: SortedMap[T, Set[Int]])

  extends Lookup[T] {

  val length = representation.foldLeft(0)((sum, e) => sum + e._2.size)

  def get(index: Int): T = representation.find(kv => kv._2.contains(index)) match {
    case Some((value, _)) => value
    case None => outofbounds(index)
  }

}

object BitSetColumn {

  def apply[T](seq: Seq[T])(implicit ordering: Ordering[T]) = new BitSetColumnBuilder[T].apply(seq)

}

class BitSetColumnBuilder[T](

  implicit ordering: Ordering[T])

  extends ColumnBuilder[T, BitSetColumn[T]] {

  def newBuilder = new WrapperBuilder[T, Result] {

    def result = Result(new BitSetColumn(wrapped.result))

    val wrapped = new BitSetBuilder[T]

  }

}


