package com.ibm.haploid

package hybriddb

package schema

package column

import java.io.ByteArrayOutputStream

import core.logger
import core.collection.mutable.FilePagedWrappedArray

object FilePagedColumn {

  def apply[T](
    seq: Seq[T],
    pagesize: Int,
    uncompressedentrysize: Int,
    expectedcompressionrate: Double)(implicit m: Manifest[T]) = {
    implicit val ordering: Ordering[T] = null
    val wrappedbuilder = FilePagedWrappedArray.newBuilder(pagesize, uncompressedentrysize, expectedcompressionrate)
    new WrappedArrayColumnBuilder[T](wrappedbuilder).apply(seq)
  }

}

