package com.ibm.haploid

package hybriddb

package schema

package column

import core.collection.mutable.ResizableWrappedArray

object PrimaryKeyColumn {

  def apply[T](seq: Seq[T])(

    implicit m: Manifest[T], ordering: Ordering[T]) = {

    implicit val pairordering: Ordering[(T, Int)] = null

    new WrappedUniqueColumnBuilder[T](
      ResizableWrappedArray.newBuilder[T],
      ResizableWrappedArray.newBuilder[(T, Int)]).apply(seq)
  }

}

