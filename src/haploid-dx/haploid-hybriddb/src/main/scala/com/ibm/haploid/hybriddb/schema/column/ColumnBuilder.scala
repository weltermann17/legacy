package com.ibm.haploid

package hybriddb

package schema

package column

import scala.collection.mutable.Builder
import scala.collection.SeqView

trait ColumnBuilder[T, C <: Column[T]] {

  def apply(seq: Seq[T]): Result = {
    val builder = newBuilder
    seq.foreach(builder += _)
    builder.result
  }

  protected[this] def newBuilder: Builder[T, Result]

  protected class Result(p: => C) extends SeqView[T, C] {

    def length = underlying.length

    def apply(index: Int) = underlying(index)

    final protected[schema] lazy val underlying: C = p

    def iterator = new Iterator[T] {

      def hasNext = i < Result.this.length

      def next = { i += 1; Result.this.apply(i - 1) }

      private[this] var i = 0

    }

  }

  protected object Result { def apply(p: => C) = new Result(p) }

}

object ColumnBuilder {

  implicit def view2column[T, C <: Column[T]](v: ColumnBuilder[T, C]#Result) = v.underlying

}

