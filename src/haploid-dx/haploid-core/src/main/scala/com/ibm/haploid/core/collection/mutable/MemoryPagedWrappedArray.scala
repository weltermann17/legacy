package com.ibm.haploid.core

package collection

package mutable

import java.io.ByteArrayOutputStream

import util.io.ByteArrayInputStream

final class MemoryPagedWrappedArray[T](
  array: Array[Byte],
  val length: Int,
  val pagesize: Int,
  val numberofpages: Int,
  val entriesperpage: Int)(

    implicit m: Manifest[T])

  extends PagedWrappedArray[T] {

  logger.debug("memory.length " + array.length)

  protected[this] def inputstream(pageindex: Int) = new ByteArrayInputStream(array, pageindex * pagesize, pagesize)

}

object MemoryPagedWrappedArray {

  def newBuilder[T](
    pagesize: Int,
    uncompressedentrysize: Int,
    expectedcompressionrate: Double)(

      implicit m: Manifest[T], ordering: Ordering[T]) = {

    new PagedWrappedArrayBuilder[T, MemoryPagedWrappedArray[T]](
      new ByteArrayOutputStream(16 * pagesize),
      pagesize,
      uncompressedentrysize,
      expectedcompressionrate) {

      protected[this] def newResult = new MemoryPagedWrappedArray(
        stream.asInstanceOf[ByteArrayOutputStream].toByteArray,
        length,
        pagesize,
        numberofpages,
        entriesperpage)

    }

  }

}

