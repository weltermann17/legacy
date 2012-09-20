package com.ibm.haploid

package hybriddb

package schema

package column

import core.collection.mutable.ResizableWrappedArray

object ArrayColumn {

  def apply[T](seq: Seq[T])(implicit m: Manifest[T]) = 
    new WrappedArrayColumnBuilder[T](ResizableWrappedArray.newBuilder[T]).apply(seq)
  
}

