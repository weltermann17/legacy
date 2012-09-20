package com.ibm.haploid

package hybriddb

package schema

package column

import collection.mutable.Builder

/**
 * Do not use a concurrent hashmap or the like to "cache" the values, to avoid concurrency issues, f should be faster than that,
 * try to implement f in a "fork/join" pattern if appropriate.
 */
final class FunctionColumn[T] private[column] (

  @transient private[this] val f: Int => T,

  val length: Int)

  extends Column[T] {

  def get(index: Int) = f(index)

}

object FunctionColumn {

  def apply[T](seq: Seq[T])(f: Int => T): FunctionColumn[T] = {
    new FunctionColumnBuilder[T](f).apply(seq)
  }

}

class FunctionColumnBuilder[T](f: Int => T)

  extends ColumnBuilder[T, FunctionColumn[T]] {

  def newBuilder = new WrapperBuilder[T, Result] {

    def result = Result(new FunctionColumn(f, wrapped.result))

    val wrapped = new CountingBuilder

  }

}

