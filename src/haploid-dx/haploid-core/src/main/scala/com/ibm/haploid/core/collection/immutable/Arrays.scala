package com.ibm.haploid

package core

package collection

package immutable

import scala.collection.mutable.WrappedArray

object Arrays {

  /**
   * for a WrappedArray of Pair(A, B), also returns the index if something is found.
   */
  def binarySearch[A, B](a: WrappedArray[(A, B)], v: A)(implicit ordering: Ordering[A]): Option[(B, Int)] = {
    def recurse(low: Int, high: Int): Option[(B, Int)] = (low + high) / 2 match {
      case _ if high < low ⇒ None
      case mid if 0 < ordering.compare(a(mid)._1, v) ⇒ recurse(low, mid - 1)
      case mid if 0 > ordering.compare(a(mid)._1, v) ⇒ recurse(mid + 1, high)
      case mid ⇒ Some(a(mid)._2, mid)
    }
    recurse(0, a.length - 1)
  }

  /**
   * for a WrappedArray of Pair(A, B), also returns the index if something is found.
   */
  def openBinarySearch[A, B](a: WrappedArray[(A, B)], v: A)(implicit ordering: Ordering[A]): Option[(B, Int)] = {
    def recurse(low: Int, high: Int): Option[(B, Int)] = (low + high) / 2 match {
      case mid if high < low ⇒
        println("high < low " + mid + " " + ordering.compare(a(mid)._1, v));
        Some(a(mid)._2, mid)
      case mid if 0 < ordering.compare(a(mid)._1, v) ⇒ recurse(low, mid - 1)
      case mid if 0 > ordering.compare(a(mid)._1, v) ⇒ recurse(mid + 1, high)
      case mid ⇒ Some(a(mid)._2, mid)
    }
    recurse(0, a.length - 1)
  }

  /**
   * for an Array of Pair(A, B)
   */
  def binarySearch[A, B](a: Array[(A, B)], v: A)(implicit ordering: Ordering[A]): Option[B] = {
    def recurse(low: Int, high: Int): Option[B] = (low + high) / 2 match {
      case _ if high < low ⇒ None
      case mid if 0 < ordering.compare(a(mid)._1, v) ⇒ recurse(low, mid - 1)
      case mid if 0 > ordering.compare(a(mid)._1, v) ⇒ recurse(mid + 1, high)
      case mid ⇒ Some(a(mid)._2)
    }
    recurse(0, a.length - 1)
  }

  /**
   * for an Array of Pair(A, B
   */
  def binarySearch[A, B](a: Array[(A, B)], v: A)(implicit m: Manifest[B]): Option[Int] = {

    class Elem(val value: (A, B))

    implicit val t2t: ((A, B)) ⇒ Ordered[Elem] = a ⇒ new Ordered[Elem] {
      def compare(b: Elem) = a._1.toString.compare(b.value._1.toString)
    }

    def inner[Y, X <% Ordered[Y]](a: Array[X], v: Y) = {
      def recurse(low: Int, high: Int): Option[Int] = (low + high) / 2 match {
        case _ if high < low ⇒ None
        case mid if a(mid) > v ⇒ recurse(low, mid - 1)
        case mid if a(mid) < v ⇒ recurse(mid + 1, high)
        case mid ⇒ Some(mid)
      }
      recurse(0, a.length - 1)
    }
    inner(a, new Elem((v, new Array[B](1)(0))))
  }

  /**
   * for an Array with Ordered of A
   */
  def binarySearch[A <% Ordered[A]](a: WrappedArray[A], v: A): Option[Int] = {
    def recurse(low: Int, high: Int): Option[Int] = (low + high) / 2 match {
      case _ if high < low ⇒ None
      case mid if a(mid) > v ⇒ recurse(low, mid - 1)
      case mid if a(mid) < v ⇒ recurse(mid + 1, high)
      case mid ⇒ Some(mid)
    }
    recurse(0, a.length - 1)
  }

  /**
   * for an Array with Ordered of A
   */
  def binarySearch[A <% Ordered[A]](a: Array[A], v: A): Option[Int] = {
    def recurse(low: Int, high: Int): Option[Int] = (low + high) / 2 match {
      case _ if high < low ⇒ None
      case mid if a(mid) > v ⇒ recurse(low, mid - 1)
      case mid if a(mid) < v ⇒ recurse(mid + 1, high)
      case mid ⇒ Some(mid)
    }
    recurse(0, a.length - 1)
  }

}

