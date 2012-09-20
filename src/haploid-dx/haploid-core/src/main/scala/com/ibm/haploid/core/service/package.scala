package com.ibm.haploid

package core

import java.lang.reflect.{ Modifier, Method }
import scala.util.matching.Regex

/**
 * Some tools to ease the use the Java reflection api in Scala.
 */
package object service {

  type Result[A] = Either[Throwable, A]
  
  object Success {
    def apply[A](success: A) = Right[Throwable, A](success)
    def unapply[A](result: Either[Throwable, A]) = result match {
      case Left(_) => None
      case Right(success) => Some(success)
    }
  }

  object Failure {
    def apply[A](throwable: Throwable) = Left[Throwable, A](throwable)
    def unapply[A](result: Either[Throwable, A]) = result match {
      case Left(throwable) => Some(throwable)
      case Right(_) => None
    }
  }

}
