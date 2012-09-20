package com.ibm.haploid.core

package collection

package mutable

import java.io.{ RandomAccessFile, FileOutputStream, BufferedOutputStream }

import file.temporaryFile
import util.io.ByteArrayInputStream

final class FilePagedWrappedArray[T](
  private[this] var source: RandomAccessFile,
  val length: Int,
  val pagesize: Int,
  val numberofpages: Int,
  val entriesperpage: Int)(

    implicit m: Manifest[T])

  extends PagedWrappedArray[T] {

  logger.debug("file.length " + source.length + ", length " + length)

  protected[this] def inputstream(pageindex: Int) = {
    val array = synchronized {
      source.seek(pageindex * pagesize)
      val array = new Array[Byte](pagesize)
      source.readFully(array)
      array
    }
    new ByteArrayInputStream(array)
  }

}

object FilePagedWrappedArray {

  def newBuilder[T](
    pagesize: Int,
    uncompressedentrysize: Int,
    expectedcompressionrate: Double)(

      implicit m: Manifest[T], ordering: Ordering[T]) = {

    val file = temporaryFile

    new PagedWrappedArrayBuilder[T, FilePagedWrappedArray[T]](
      new BufferedOutputStream(new FileOutputStream(file), util.io.buffersize),
      pagesize,
      uncompressedentrysize,
      expectedcompressionrate) {

      protected[this] def newResult = new FilePagedWrappedArray(
        new RandomAccessFile(file, "r"),
        length,
        pagesize,
        numberofpages,
        entriesperpage)
    }

  }

}

