package com.ibm.haploid.core

package collection

package mutable

import scala.collection.mutable.{ Builder, WrappedArray }

/**
 * A simplified version of scala.collection.mutable.ResizableArray that uses a real Array[T] internally and where toArray returns the shrinked to exact size copy of the inner array. A call to toArray will reset the inner array after copying.
 */
final class ResizableWrappedArray[A](
  initialcapacity: Int)(implicit m: Manifest[A])

  extends WrappedArray[A] {

  def this()(implicit m: Manifest[A]) = this(16)

  val elemManifest = m

  def apply(index: Int) = arr(index)

  def update(index: Int, elem: A) = {
    capacity(index + 1)
    arr.update(index, elem)
    if (index >= maxindex) {
      maxindex = index + 1
    }
  }

  /**
   * Careful, this has the side effect of clearing the inner array after copying it, therefore, call it only once at the end.
   */
  def array = {
    if (0 < callcount) logger.warning("Called ResizableWrappedArray.array more than once (" + callcount + 1 + ")")
    callcount += 1
    val copy = new Array[A](maxindex)
    compat.Platform.arraycopy(arr, 0, copy, 0, maxindex)
    clear
    copy
  }

  def length = maxindex

  def clear = {
    arr = new Array[A](initialcapacity)
    maxindex = 0
  }

  @inline private[this] def capacity(n: Int) {
    if (n > arr.length) {
      var newsize = arr.length * 2
      while (n > newsize) newsize *= 2
      val copy = new Array[A](newsize)
      compat.Platform.arraycopy(arr, 0, copy, 0, maxindex)
      arr = copy
    }
  }

  private[this] var callcount = 0

  private[this] var arr = new Array[A](initialcapacity)

  private[this] var maxindex = 0

}

object ResizableWrappedArray {

  def newBuilder[T](implicit m: Manifest[T]) = new ResizableWrappedArrayBuilder

}

final class ResizableWrappedArrayBuilder[T](implicit m: Manifest[T])

  extends Builder[T, ResizableWrappedArray[T]] {

  def +=(elem: T) = {
    array.update(i, elem)
    i += 1
    this
  }

  def result = array

  def clear = { array.clear; i = 0 }

  private[this] val array = new ResizableWrappedArray[T]

  private[this] var i = 0

}

