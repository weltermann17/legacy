package com.ibm.haploid

package hybriddb

package schema

package column

import collection.SortedMap 
import collection.mutable.{ Builder, WrappedArray }
import core.collection.immutable.WrappedArraySortedMap

class UniqueBuilder[T](

  array: Builder[T, WrappedArray[T]],

  sortedmap: Builder[(T, Int), WrappedArray[(T, Int)]])(

    implicit m: Manifest[T], ordering: Ordering[T])

  extends Builder[T, (WrappedArray[T], SortedMap[T, Int])] {

  def +=(elem: T) = { array += elem; sortedmap += ((elem, i)); i += 1; this }

  def result: (WrappedArray[T], SortedMap[T, Int]) = {
    (array.result, WrappedArraySortedMap(sortedmap.result))
  }

  def clear = { array.clear; sortedmap.clear }
  
  private[this] var i = 0

}

