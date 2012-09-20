package com.ibm.haploid

package hybriddb

package schema

package column

import scala.collection.mutable.{ Builder, WrappedArray }

class WrappedArrayColumn[T] private[column] (

  protected[this] val array: WrappedArray[T])

  extends Column[T] {

  val length = array.length

  def get(index: Int) = array(index)

}

final class WrappedArrayColumnBuilder[T](

  wrappedbuilder: Builder[T, WrappedArray[T]])(

    implicit m: Manifest[T])

  extends ColumnBuilder[T, WrappedArrayColumn[T]] {

  def newBuilder = new WrapperBuilder[T, Result] {

    def result = Result(new WrappedArrayColumn(wrapped.result))

    val wrapped = wrappedbuilder

  }

}
