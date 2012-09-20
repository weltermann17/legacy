//package com.ibm.haploid
//
//package hybriddb
//
//package schema
//
//package column
//
//import collection.SortedMap
//
//final class UniqueStringColumn[@specialized(Int, Long, Double) T] private[column] (
//
//  protected[this] val representation: SortedMap[T, Int],
//
//  array: Array[T])
//
//  extends ArrayColumn[T](array) with Unique[T] with Searchable[T] {
//
//}
//
//object UniqueStringColumn {
//
//  def apply[T](seq: Seq[T])(implicit m: Manifest[T], ordering: Ordering[T]) = new UniqueStringColumnBuilder[T].apply(seq)
//
//}
//
//class UniqueStringColumnBuilder[@specialized(Int, Long, Double) T](
//
//  implicit m: Manifest[T], ordering: Ordering[T])
//
//  extends ColumnBuilder[T, UniqueStringColumn[T]] {
//
//  def newBuilder = new WrapperBuilder[T, Result] {
//
//    def result = wrapped.result match { case (a, r) => Result(new UniqueStringColumn(r, a)) }
//
//    val wrapped = new UniqueBuilder[T]
//
//  }
//
//}
