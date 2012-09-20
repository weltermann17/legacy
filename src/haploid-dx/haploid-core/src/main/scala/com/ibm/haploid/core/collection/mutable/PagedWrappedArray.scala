package com.ibm.haploid

package core

package collection

package mutable

import java.io.{ ObjectInputStream, InputStream }
import java.util.zip.GZIPInputStream

import scala.collection.mutable.WrappedArray

import util.io.ByteArrayOutputStream

abstract class PagedWrappedArray[T](

  implicit m: Manifest[T])

  extends WrappedArray[T]

  with Paged[T] {

  def apply(index: Int) = get(index)

  def array = throw new UnsupportedOperationException

  val elemManifest = m

  protected[this] def loadPage(pageindex: Int): Page[T] = {
    val in = new ObjectInputStream(new GZIPInputStream(inputstream(pageindex)))
    new Page(in.readObject.asInstanceOf[Array[T]], pageindex)
  }

  protected[this] def inputstream(pageindex: Int): InputStream

}

