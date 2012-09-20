package com.ibm.haploid

package core

package service

/**
 *
 */
trait Service[I, O] {

  def doService(input: I): Result[O]

  def apply(input: I): Result[O] = try { doService(input) } catch { case e: Throwable ⇒ Failure(e) }

}

/**
 * This shows how to use the above trait.
 */
object Sample {

  object TestService extends Service[String, Int] {

    def doService(s: String): Result[Int] = Success(s.toInt)

  }

  println(TestService("1"))
  println(TestService("A"))

}

