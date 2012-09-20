package com.ibm.haploid.core

package collection

package mutable

/**
 * A simplified version of scala.collection.mutable.ResizableArray that uses a real Array[T] internally and where toArray returns the inner array, be aware that the inner array usually has a larger length due to its buffering character.
 */
class ResizableArray[@specialized(Int) A](initialsize: Int)(implicit m: Manifest[A]) {

  protected var array: Array[A] = new Array[A](initialsize)
  protected var size0: Int = 0

  def toArray = array

  def length = size0 + 1

  def apply(idx: Int) = array(idx)

  def update(idx: Int, elem: A) = {
    ensureSize(idx + 1)
    array.update(idx, elem)
    if (idx > size0) size0 = idx
  }

  def clear = {
    array = new Array[A](initialsize)
    size0 = 0
  }

  protected[this] def ensureSize(n: Int) {
    if (n > array.length) {
      var newsize = array.length * 2
      while (n > newsize) newsize *= 2
      val newar: Array[A] = new Array[A](newsize)
      compat.Platform.arraycopy(array, 0, newar, 0, size0)
      array = newar
    }
  }

}

