package com.ibm.haploid

package hybriddb

package schema

package column

import core.collection.mutable.FilePagedWrappedArray
import core.collection.PairOrdering

object FilePagedUniqueColumn {

  def apply[T](seq: Seq[T],
    pagesize1: Int,
    uncompressedentrysize1: Int,
    expectedcompressionrate1: Double,
    pagesize2: Int,
    uncompressedentrysize2: Int,
    expectedcompressionrate2: Double)(

      implicit m: Manifest[T], ordering: Ordering[T]) = {

    implicit val pairordering = PairOrdering[T, Int]
    
    new WrappedUniqueColumnBuilder[T](
      FilePagedWrappedArray.newBuilder[T](pagesize1, uncompressedentrysize1, expectedcompressionrate1),
      FilePagedWrappedArray.newBuilder[(T, Int)](pagesize2, uncompressedentrysize2, expectedcompressionrate2)).apply(seq)
  }

}

