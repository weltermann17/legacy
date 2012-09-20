package com.ibm.haploid

package hybriddb

package schema

package column

import scala.collection.mutable.Builder

trait WrapperBuilder[T, B]

  extends Builder[T, B] {

  def +=(elem: T) = { wrapped += elem; this }

  def result: B

  def clear = wrapped.clear

  val wrapped: Builder[T, _]

}

