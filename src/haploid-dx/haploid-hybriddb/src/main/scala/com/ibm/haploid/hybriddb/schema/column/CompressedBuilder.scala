package com.ibm.haploid

package hybriddb

package schema

package column

import scala.collection.{ Set, SortedMap }
import scala.collection.mutable.{ Builder, HashSet => MutableSet, WrappedArray }
import scala.collection.mutable.HashMap

import core.logger
import core.collection.immutable.WrappedArraySortedMap
import core.collection.PairOrdering

final class CompressedBuilder[T](

  implicit m: Manifest[T], ordering: Ordering[T])

  extends Builder[T, (SortedMap[T, Set[Int]], Array[Int], Array[T])] {

  def +=(elem: T) = {
    keys.get(elem) match {
      case None => val s = new MutableSet[Int]; s += i; keys.put(elem, s)
      case Some(s) => s += i
    }
    i += 1
    if (0 == i % 100000) logger.debug(i.toString)
    this
  }

  def result = {
    val values = new Array[Int](i)
    val distinctvalues = new Array[T](keys.size)
    var j = 0; keys.keySet.foreach { k => distinctvalues.update(j, k); j += 1 }
    var k = 0; keys.values.foreach { v => v.foreach(values.update(_, k)); k += 1 }
    implicit val pairordering = PairOrdering[T, MutableSet[Int]]
    (WrappedArraySortedMap(WrappedArray.make[(T, Set[Int])](keys.toArray.sorted)), values, distinctvalues)
  }

  def clear = { keys.clear; i = 0 }

  private[this] var i = 0

  private[this] val keys = new HashMap[T, MutableSet[Int]]

}

