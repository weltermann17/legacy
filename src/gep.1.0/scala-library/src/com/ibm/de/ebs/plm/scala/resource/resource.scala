package com.ibm.de.ebs.plm.scala

import scala.util.continuations._

/**
 * Implements the "Disposable pattern" which is "sort-of" providing destructors for languages with garbage collection.
 *
 * @see [[http://blog.omega-prime.co.uk/?p=17 Inspired by "using" keyword in C#]]
 */

package object resource {

  /**
   * Has a method "dispose" that will be *automatically* called at the end of a "using" block.
   */
  trait Disposable[S] {
    def dispose
  }

  trait ContextType[T]

  def forceContextType[T]: ContextType[T] = null

  private def use[S <% Disposable[S], T: ContextType](what: S)(block: S => T): T = {
    try {
      block(what)
    } catch {
      case e: java.lang.OutOfMemoryError =>
        e.printStackTrace; println
        println("Resources.Using : " + e)
        val runtime = Runtime.getRuntime
        println("Resources.Using : memory free/max/total : " + runtime.freeMemory + " " + runtime.maxMemory + " " + runtime.totalMemory)
        println("Resources.Using : Program will abort now."); println
        runtime.exit(-1)
        throw e
      case e =>
        throw e
    } finally {
      what.dispose
    }
  }

  /**
   * @return The input wrapped as a Disposable, necessary for all things you want to dispose after "using".
   */
  def disposable[S <% Disposable[S], T: ContextType](what: S): S @cps[T] = shift(use[S, T](what))

  /**
   * Syntactic sugar to support code like
   * {{{
   * implicit val _ = forceContextType[Unit]
   * using {
   *     val representation = disposable(perLocation(location, filesperlocation))
   *     val zipin = disposable(new ZipInputStream(representation.getStream))
   *     var zipentry: ZipEntry = null
   *     while (null != { zipentry = zipin.getNextEntry; zipentry }) {
   *         addZipEntry(zipentry.getName) {
   *             copyBytes(zipin, zipout)
   * 	           files.append(zipentry.getName)
   * 	       }
   *     }
   * }
   * }}}
   * @return T of forceContextType[T]
   */
  def using[T](block: => T @cps[T]): T = reset { block }

  // implicit conversions

  implicit def a[S](what: S { def dispose() }): Disposable[S] = {
    new Disposable[S] {
      def dispose = what.dispose
    }
  }

  implicit def b[S](what: S { def close() }): Disposable[S] = {
    new Disposable[S] {
      def dispose = {
        try {
          what.close
        } catch {
          case e =>
        }
      }
    }
  }

  implicit def c[S](what: S { def close(); def closeEntry(); def finish() }): Disposable[S] = {
    new Disposable[S] {
      def dispose = {
        try {
          what.finish
        } catch {
          case e =>
        }
      }
    }
  }

  implicit def d[S](what: S { def exhaust(): Long; def release() }): Disposable[S] = {
    new Disposable[S] {
      def dispose = {
        what.exhaust
        what.release
      }
    }
  }

  implicit def e[S](what: S { def close }): Disposable[S] = {
    new Disposable[S] {
      def dispose = {
        try {
          what.close
        } catch {
          case e =>
        }
      }
    }
  }

  implicit def f[A](what: Stream[A]): Disposable[Stream[A]] = {
    new Disposable[Stream[A]] {
      def dispose = {
        what.force
        ()
      }
    }
  }

  implicit def g[S](what: S { def close(); def finish() }): Disposable[S] = {
    new Disposable[S] {
      def dispose = {
        try {
          what.finish
          what.close
        } catch {
          case e =>
        }
      }
    }
  }

}
