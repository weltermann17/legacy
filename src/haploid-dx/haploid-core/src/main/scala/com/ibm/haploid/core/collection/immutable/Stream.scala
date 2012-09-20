package com.ibm.haploid

package core

package collection

package immutable

object Stream {

  /**
<<<<<<< HEAD
   * The implementation of scala.collection.immutable.Stream is very costly (using volatile and synchronized),
=======
   * The implementation in scala.collection.immutable.Stream is very costly (using volatile and synchronized),
>>>>>>> 7a6e0b51c1dad901d4c0bbcadcffeb086d90cc83
   * this implementation here avoids it, but is therefore not thread-safe, yet it is 50x faster and uses 90% less memory.
   */
  final class Cons[+A](hd: A, tl: => Stream[A]) extends Stream[A] { self =>

    override val isEmpty = false

    override val head = hd

    override def tail: Stream[A] = tl

    protected val tailDefined = true

  }

  object cons {

    def apply[A](hd: A, tl: => Stream[A]) = new Cons(hd, tl)

  }

}

