package com.ibm.haploid

package core

package collection

/**
 *
 */
trait PairOrdering

/**
 *
 */
object PairOrdering {

  def apply[A, B](implicit ordering: Ordering[A]): Ordering[(A, B)] = new Ordering[(A, B)] with PairOrdering {

    def compareWithNull(x: (A, B), y: (A, B)) = (x, y) match {
      case (null, null) ⇒ 1
      case (_, null) ⇒ -1
      case (null, _) ⇒ 1
      case (x, y) ⇒ ordering.compare(x._1, y._1)
    }

    @inline def compare(x: (A, B), y: (A, B)) = ordering.compare(x._1, y._1)

  }

}
