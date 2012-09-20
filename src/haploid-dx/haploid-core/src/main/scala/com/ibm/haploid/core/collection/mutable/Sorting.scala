package com.ibm.haploid

package core

package collection

package mutable

import scala.collection.mutable.WrappedArray

import immutable.Arrays.binarySearch

object Sorting {

  /**
   * QuickSort on a WrappedArray with an implicit ordering on (A, B).
   */
  def quickSort[A, B](array: WrappedArray[(A, B)], off: Int, len: Int)(implicit ordering: Ordering[(A, B)]) = {

    import ordering._

    @inline def swap(a: Int, b: Int) = {
      val t = array(a)
      array(a) = array(b)
      array(b) = t
    }

    @inline def vecswap(_a: Int, _b: Int, n: Int) = {
      var a = _a
      var b = _b
      var i = 0
      while (i < n) {
        swap(a, b)
        i += 1
        a += 1
        b += 1
      }
    }

    @inline def med3(a: Int, b: Int, c: Int) = {
      if (array(a) < array(b)) {
        if (array(b) < array(c)) b else if (array(a) < array(c)) c else a
      } else {
        if (array(b) > array(c)) b else if (array(a) > array(c)) c else a
      }
    }

    def sort2(off: Int, len: Int): Unit = {
      if (len < 7) {
        var i = off
        while (i < len + off) {
          var j = i
          while (j > off && array(j - 1) > array(j)) {
            swap(j, j - 1)
            j -= 1
          }
          i += 1
        }
      } else {
        var m = off + (len >> 1)
        if (len > 7) {
          var l = off
          var n = off + len - 1
          if (len > 40) {
            var s = len / 8
            l = med3(l, l + s, l + 2 * s)
            m = med3(m - s, m, m + s)
            n = med3(n - 2 * s, n - s, n)
          }
          m = med3(l, m, n)
        }
        val v = array(m)

        var a = off
        var b = a
        var c = off + len - 1
        var d = c
        var done = false
        while (!done) {
          while (b <= c && array(b) <= v) {
            if (array(b) == v) {
              swap(a, b)
              a += 1
            }
            b += 1
          }
          while (c >= b && array(c) >= v) {
            if (array(c) == v) {
              swap(c, d)
              d -= 1
            }
            c -= 1
          }
          if (b > c) {
            done = true
          } else {
            swap(b, c)
            c -= 1
            b += 1
          }
        }

        val n = off + len
        var s = scala.math.min(a - off, b - a)
        vecswap(off, b - s, s)
        s = scala.math.min(d - c, n - d - 1)
        vecswap(b, n - s, s)

        s = b - a
        if (s > 1)
          sort2(off, s)
        s = d - c
        if (s > 1)
          sort2(n - s, s)
      }
    }
    sort2(off, len)
  }

  /**
   * QuickSort on an Array of T.
   */
  def quickSort[A, B](array: Array[A], off: Int, len: Int)(implicit ordering: Ordering[A]) = {

    import ordering._

    @inline def swap(a: Int, b: Int) = {
      val t = array(a)
      array(a) = array(b)
      array(b) = t
    }

    @inline def vecswap(_a: Int, _b: Int, n: Int) = {
      var a = _a
      var b = _b
      var i = 0
      while (i < n) {
        swap(a, b)
        i += 1
        a += 1
        b += 1
      }
    }

    @inline def med3(a: Int, b: Int, c: Int) = {
      if (array(a) < array(b)) {
        if (array(b) < array(c)) b else if (array(a) < array(c)) c else a
      } else {
        if (array(b) > array(c)) b else if (array(a) > array(c)) c else a
      }
    }

    def sort2(off: Int, len: Int): Unit = {
      if (len < 7) {
        var i = off
        while (i < len + off) {
          var j = i
          while (j > off && array(j - 1) > array(j)) {
            swap(j, j - 1)
            j -= 1
          }
          i += 1
        }
      } else {
        var m = off + (len >> 1)
        if (len > 7) {
          var l = off
          var n = off + len - 1
          if (len > 40) {
            var s = len / 8
            l = med3(l, l + s, l + 2 * s)
            m = med3(m - s, m, m + s)
            n = med3(n - 2 * s, n - s, n)
          }
          m = med3(l, m, n)
        }
        val v = array(m)

        var a = off
        var b = a
        var c = off + len - 1
        var d = c
        var done = false
        while (!done) {
          while (b <= c && array(b) <= v) {
            if (array(b) == v) {
              swap(a, b)
              a += 1
            }
            b += 1
          }
          while (c >= b && array(c) >= v) {
            if (array(c) == v) {
              swap(c, d)
              d -= 1
            }
            c -= 1
          }
          if (b > c) {
            done = true
          } else {
            swap(b, c)
            c -= 1
            b += 1
          }
        }

        val n = off + len
        var s = scala.math.min(a - off, b - a)
        vecswap(off, b - s, s)
        s = scala.math.min(d - c, n - d - 1)
        vecswap(b, n - s, s)

        s = b - a
        if (s > 1)
          sort2(off, s)
        s = d - c
        if (s > 1)
          sort2(n - s, s)
      }
    }
    sort2(off, len)
  }

}

