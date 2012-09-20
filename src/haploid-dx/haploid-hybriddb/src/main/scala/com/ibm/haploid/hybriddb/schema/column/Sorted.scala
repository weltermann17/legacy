package com.ibm.haploid

package hybriddb

package schema

package column

import collection.{ SortedSet, SortedMap }

trait Sorted[T, B]

  extends Column[T] with SortedMap[T, B] {

  protected[this] val representation: SortedMap[T, B]

  def +[B1 >: B](kv: (T, B1)) = representation + kv

  def -(k: T) = representation - k

  def iterator = representation.iterator

  def rangeImpl(f: Option[T], u: Option[T]) = representation.rangeImpl(f, u)

  def ordering = representation.ordering

  override def toList = representation.toList
  
}

