package com.ibm.haploid

package hybriddb

package schema

package column

import core.collection.mutable.MemoryPagedWrappedArray

object MemoryPagedPrimaryKeyColumn {

  def apply[T](seq: Seq[T],
    pagesize1: Int,
    uncompressedentrysize1: Int,
    expectedcompressionrate1: Double,
    pagesize2: Int,
    uncompressedentrysize2: Int,
    expectedcompressionrate2: Double)(

      implicit m: Manifest[T], ordering: Ordering[T]) = {

    implicit val pairordering: Ordering[(T, Int)] = null

    new WrappedUniqueColumnBuilder[T](
      MemoryPagedWrappedArray.newBuilder[T](pagesize1, uncompressedentrysize1, expectedcompressionrate1),
      MemoryPagedWrappedArray.newBuilder[(T, Int)](pagesize2, uncompressedentrysize2, expectedcompressionrate2)).apply(seq)
  }

}

