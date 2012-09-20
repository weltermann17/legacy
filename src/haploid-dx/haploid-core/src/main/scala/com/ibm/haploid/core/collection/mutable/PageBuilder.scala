package com.ibm.haploid

package core

package collection

package mutable

import java.io.ObjectOutputStream
import java.util.zip.GZIPOutputStream

import scala.collection.mutable.Builder

import collection.mutable.Sorting.quickSort
import util.io.ByteArrayOutputStream

final class PageBuilder[T](
  pagesize: Int,
  entriesperpage: Int,
  expectedcompressionrate: Double)(

    implicit m: Manifest[T], ordering: Ordering[T])

  extends Builder[T, Array[Byte]] {

  logger.debug("PageBuilder numberofpages " + numberofpages + ", pagesize " + pagesize + ", entriesperpage " + entriesperpage)

  def +=(elem: T) = { array.update(i, elem); i += 1; this }

  def result = {
    write
    checkcompression
    pagecount += 1
    i = 0
    buffer
  }

  def clear = { i = 0; pagecount = 0 }

  def getMostAggressiveCompressionRate = mostaggressivecompressionrate

  def numberofpages = pagecount

  @inline private[this] def checkcompression = if (logger.isDebugEnabled)
    mostaggressivecompressionrate = scala.math.min(
      expectedcompressionrate * (bos.getPosition.toDouble / pagesize),
      mostaggressivecompressionrate)

  private[this] def write = {
    try {
      bos.reset
      val gzip = new GZIPOutputStream(bos) {
        `def`.setLevel(java.util.zip.Deflater.BEST_SPEED)
      }
      val out = new ObjectOutputStream(gzip)
      out.writeObject(array)
      out.close
    } catch {
      case _ ⇒
        if (0 < pagecount) warning
        bos.reset
        val gzip = new GZIPOutputStream(bos) {
          `def`.setLevel(java.util.zip.Deflater.BEST_COMPRESSION)
        }
        val out = new ObjectOutputStream(gzip)
        out.writeObject(array)
        out.close
    }
  }

  private[this] def warning = logger.debug("pagesize exceeded " + pagesize + ", retrying BEST_COMPRESSION, mostaggressivecompressionrate " + mostaggressivecompressionrate + ", expectedcompressionrate " + expectedcompressionrate)

  private[this] val array = new Array[T](entriesperpage)

  private[this] val buffer = new Array[Byte](pagesize)

  private[this] val bos = new ByteArrayOutputStream(buffer)

  private[this] var mostaggressivecompressionrate = expectedcompressionrate

  private[this] var i = 0

  private[this] var pagecount = 0

}

