package com.ibm.haploid

package core

package collection

package mutable

import java.io.{ File, OutputStream, ObjectInputStream, BufferedInputStream, FileInputStream }
import java.util.zip.GZIPInputStream

import scala.collection.mutable.{ Builder, ListBuffer }

abstract class PagedWrappedArrayBuilder[T, P <: PagedWrappedArray[T]](
  protected[this] val stream: OutputStream,
  pagesize: Int,
  uncompressedentrysize: Int,
  expectedcompressionrate: Double)(

    implicit m: Manifest[T], ordering: Ordering[T])

  extends Builder[T, P] {

  def +=(elem: T) = {
    if (0 < i) check
    (if (needsorting) filebuilder else pagebuilder) += elem
    i += 1
    this
  }

  private[this] implicit val tapeordering = new Ordering[(T, Tape)] {
    def compare(x: (T, Tape), y: (T, Tape)) = ordering.compare(x._1, y._1)
  }

  def result = {
    flush
    if (needsorting) util.time.infoNanos("sorting") {
      i = 0
      needsorting = false
      files.foreach { f => tapes += new Tape(f) }
      while (!tapes.isEmpty) {
        tapes.map(_.lookahead).min match {
          case Some(v) => v match { case (elem, tape) => this += elem; tape.advance }
          case _ =>
        }
        tapes.filter(_.exhausted).foreach { tape => tape.close; tapes -= tape }
      }
      flush
    }
    compressioninfo
    stream.close
    newResult
  }

  def clear = throw new UnsupportedOperationException

  def length = i

  def numberofpages = pagebuilder.numberofpages

  protected[this] def newResult: P

  protected[this] val entriesperpage = (pagesize.toDouble / (uncompressedentrysize * expectedcompressionrate)).toInt

  private[this] var needsorting = ordering match {
    case o: PairOrdering if o != null => true
    case _ => false
  }

  private[this] def check = if (0 == i % ((if (needsorting) K else 1) * entriesperpage)) flush

  private[this] def flush = if (needsorting) files += filebuilder.result else stream.write(pagebuilder.result)

  private[this] def compressioninfo = logger.debug("most aggressive compression rate " + pagebuilder.getMostAggressiveCompressionRate + ", current compression rate " + expectedcompressionrate)

  private[this] var i = 0

  private[this] val pagebuilder = new PageBuilder[T](pagesize, entriesperpage, expectedcompressionrate)

  private[this] lazy val filebuilder = new FileBuilder[T](pagesize * K, entriesperpage * K, expectedcompressionrate)

  private[this] lazy val files = new ListBuffer[File]

  private[this] lazy val tapes = new ListBuffer[Tape]

  private class Tape(file: File) {
    
    def exhausted = None == cursor

    def lookahead: Option[(T, Tape)] = cursor match { case None => None case Some(c) => Some(c, this) }

    def advance: Unit = cursor = read

    def close = { in.close; file.delete }

    private[this] val in = {
      new ObjectInputStream(
        new BufferedInputStream(
          new GZIPInputStream(
            new FileInputStream(file),
            pagesize),
          pagesize))
    }

    private[this] def read: Option[T] = { cursor = try { in.readObject.asInstanceOf[T] match { case null => None case elem => Some(elem) } } catch { case _ => None }; cursor }

    private[this] var cursor: Option[T] = read

  }

  private[this] val K = 256

}

