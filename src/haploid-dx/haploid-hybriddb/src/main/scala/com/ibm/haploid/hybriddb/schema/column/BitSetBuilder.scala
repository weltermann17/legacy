package com.ibm.haploid

package hybriddb

package schema

package column

import collection.{ BitSet, Set, SortedMap }
import collection.mutable.{ Builder, HashMap, BitSet => MutableBitSet, WrappedArray }

import core.collection.immutable.WrappedArraySortedMap
import core.collection.PairOrdering

final class BitSetBuilder[T](

  implicit ordering: Ordering[T])

  extends Builder[T, SortedMap[T, Set[Int]]] {

  def +=(elem: T) = {
    val b = bitsets.get(elem) match {
      case None => val b = new MutableBitSet; bitsets += (elem -> b); b
      case Some(b) => b
    }
    b += i
    i += 1
    this
  }

  def result: SortedMap[T, Set[Int]] = {
    implicit val pairordering = PairOrdering[T, MutableBitSet]
    
    WrappedArraySortedMap(
      WrappedArray.make[(T, Set[Int])](bitsets.map(kv => (kv._1, kv._2)).toArray.sorted))
  }

  def clear = { bitsets.clear; i = 0 }

  private[this] var i = 0

  private[this] val bitsets = new HashMap[T, MutableBitSet]

}

