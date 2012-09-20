package com.ibm.haploid

package hybriddb

package schema

package column

import scala.collection.mutable.{ Builder, WrappedArray }

import collection.SortedMap

final class WrappedUniqueColumn[T] private[column] (

  array: WrappedArray[T],

  protected[this] val representation: SortedMap[T, Int])

  extends WrappedArrayColumn[T](array) with Unique[T] {

}

class WrappedUniqueColumnBuilder[T](

  array: Builder[T, WrappedArray[T]],

  sortedmap: Builder[(T, Int), WrappedArray[(T, Int)]])(

    implicit m: Manifest[T], ordering: Ordering[T])

  extends ColumnBuilder[T, WrappedUniqueColumn[T]] {

  def newBuilder = new WrapperBuilder[T, Result] {

    def result = wrapped.result match { case (a, r) => Result(new WrappedUniqueColumn(a, r)) }

    val wrapped = new UniqueBuilder[T](array, sortedmap)

  }

}

