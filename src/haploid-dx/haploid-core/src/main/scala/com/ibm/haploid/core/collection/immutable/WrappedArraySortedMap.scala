package com.ibm.haploid

package core

package collection

package immutable

import scala.collection.{ IndexedSeqOptimized, Set }
import scala.collection.immutable.SortedMap
import scala.collection.mutable.WrappedArray
import scala.util.Sorting.quickSort

import Arrays._
import collection.mutable.{ Page, PagedWrappedArray }

trait WrappedArraySortedMap[A, B]

  extends SortedMap[A, B] {

  outer ⇒

  def -(key: A): SortedMap[A, B] = new Minus(this, ordering, key)

  def get(key: A): Option[B] = binarySearch(wrappedarray, key) match {
    case None ⇒ None
    case Some(found) ⇒ Some(found._1)
  }

  def iterator = wrappedarray.iterator

  def rangeImpl(f: Option[A], u: Option[A]): SortedMap[A, B] = new Range(this, ordering, f, u)

  override def toSeq = new Seq[(A, B)] {

    val length = wrappedarray.length

    def apply(index: Int) = wrappedarray(index)

    def iterator = wrappedarray.iterator

    override def indexOf[B1 >: (A, B)](elem: B1): Int = binarySearch(wrappedarray, elem.asInstanceOf[(A, B)]._1) match {
      case None ⇒ -1
      case Some(found) ⇒ found._2
    }

  }

  protected[this] val wrappedarray: WrappedArray[(A, B)]

  /**
   * For implementing '-' method.
   */
  private class Minus(

    protected[this] val wrapped: WrappedArraySortedMap[A, B],

    implicit val ordering: Ordering[A],

    without: A)

    extends WrappedArraySortedMap[A, B] {

    override def get(key: A) = key match {
      case k if k == without ⇒ None
      case k ⇒ super.get(k)
    }

    override def iterator = new Iterator[(A, B)] {

      def hasNext = i < wrappedarray.length - 1

      def next: (A, B) = {
        i += 1
        wrappedarray(i - 1) match {
          case (a, b) if a == without ⇒
            i += 1
            wrappedarray(i - 1)
          case ab ⇒ ab
        }
      }

      private[this] var i = 0

    }

    override def rangeImpl(f: Option[A], u: Option[A]) = throw new UnsupportedOperationException

    protected[this] val wrappedarray = outer.wrappedarray

  }

  /**
   * For implementing 'rangeImpl' method.
   */
  private class Range(

    protected[this] val wrapped: WrappedArraySortedMap[A, B],

    implicit val ordering: Ordering[A],

    f: Option[A],

    u: Option[A])

    extends WrappedArraySortedMap[A, B] {

    self ⇒

    import ordering._

    override def get(key: A) = key match {
      case k if f.isDefined && k < f.get ⇒ None
      case k if u.isDefined && k > u.get ⇒ None
      case _ ⇒ super.get(key)
    }

    override def iterator = new Iterator[(A, B)] {

      def hasNext = i <= to

      def next: (A, B) = { i += 1; wrappedarray(i - 1) }

      private[this] val from = f match {
        case None ⇒ 0
        case Some(f) ⇒ openBinarySearch(wrappedarray, f) match {
          case None ⇒ -1
          case Some(found) ⇒ found._2
        }
      }

      private[this] val to = u match {
        case None ⇒ wrappedarray.length
        case Some(u) ⇒ openBinarySearch(wrappedarray, u) match {
          case None ⇒ -1
          case Some(found) ⇒ found._2
        }
      }

      private[this] var i = from

    }

    protected[this] val wrappedarray = outer.wrappedarray

  }
  WrappedArraySortedMap
}

object WrappedArraySortedMap {

  def apply[A, B](array: WrappedArray[(A, B)])(

    implicit keyordering: Ordering[A]) = new WrappedArraySortedMap[A, B] {

    implicit val ordering = keyordering

    protected[this] val wrappedarray = array

  }

}

