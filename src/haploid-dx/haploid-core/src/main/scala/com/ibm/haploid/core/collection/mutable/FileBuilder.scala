package com.ibm.haploid

package core

package collection

package mutable

import java.io.{ OutputStream, ObjectOutputStream, FileOutputStream, File, BufferedOutputStream }
import java.util.zip.GZIPOutputStream

import scala.collection.mutable.Builder

import collection.mutable.Sorting.quickSort
import file.temporaryFile

final class FileBuilder[T](
  pagesize: Int,
  entriesperpage: Int,
  expectedcompressionrate: Double)(

    implicit m: Manifest[T], ordering: Ordering[T])

  extends Builder[T, File] {

  def +=(elem: T) = { array.update(i, elem); i += 1; this }

  def result = {
    quickSort(array, 0, i)
    val f = temporaryFile
    flush(new BufferedOutputStream(new FileOutputStream(f), pagesize))
    f
  }

  def clear = i = 0

  private[this] def flush(f: OutputStream) = {
    val gzip = new GZIPOutputStream(f) {
      `def`.setLevel(java.util.zip.Deflater.BEST_SPEED)
    }
    val out = new ObjectOutputStream(gzip)
    for (j <- 0 until i) out.writeObject(array(j))
    i = 0
    out.close
  }

  private[this] val array = new Array[T](entriesperpage)

  private[this] var i = 0

  logger.debug("FileBuilder pagesize " + pagesize + ", entriesperpage " + entriesperpage)

}

