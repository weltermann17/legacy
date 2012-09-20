package com.ibm.haploid

package hybriddb

package schema

package column

import core.collection.mutable.ResizableWrappedArray
import core.collection.PairOrdering

object UniqueColumn {

  def apply[T](seq: Seq[T])(

    implicit m: Manifest[T], ordering: Ordering[T]) = {

    implicit val pairordering = PairOrdering[T, Int]
    
    new WrappedUniqueColumnBuilder[T](
      ResizableWrappedArray.newBuilder[T],
      ResizableWrappedArray.newBuilder[(T, Int)]).apply(seq)
  }

}

