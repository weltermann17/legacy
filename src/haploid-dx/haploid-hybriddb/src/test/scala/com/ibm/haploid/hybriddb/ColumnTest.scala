package com.ibm.haploid

package hybriddb

import java.sql.Timestamp

import scala.collection.{ Map, Set }

import org.junit.Assert.assertTrue
import org.junit.Test

import util.Random._

import core.file._
import core.util.json._
import schema.column._
import schema._
import io._
import core.util.time._

import core.inject._
import core.reflect._
import core.util.time.now
import core.collection.immutable.Stream.cons

@Test private class ColumnTest {

  val N = 100000

  @Test def testBooleanColumn = {
    val l = List(true, true, false)
    val c = BooleanColumn(l)
    println("not yet forced")
    println("col " + c)
    for (i <- 0 until 3) println(c(i))
    println(c.length)
    println("col after force " + c.force)
    val m: Map[Boolean, Set[Int]] = c
    println("as map" + m)
    val d = BooleanColumn(List(false, false, false, false, true))
    println(d.force)
    val n: Map[Boolean, collection.Set[Int]] = d
    println("as map" + n)
  }

  @Test def testBitSetColumn = {
    val l = List("a", "b", "a", "a", "b", "c", "b")
    val c = BitSetColumn[String](l)
    println("not yet forced")
    println("col " + c)
    for (i <- 0 until 7) println(c(i))
    println(c.length)
    println("c after force " + c.force)
    println(c.filter("a"==).force)
    println(c.force)
    val f: BitSetColumn[String] = c
    println(f.get("a"))
    println(c.get("b"))
    val m: Map[String, collection.Set[Int]] = c
    println("as map" + m)
  }

  @Test def testBigBooleanColumn = {
    val n = N
    import core.collection.immutable.Stream.cons
    def l(i: Int): Stream[Boolean] = cons(nextBoolean, if (1 < i) l(i - 1) else Stream.empty)
    val c = BooleanColumn(l(n))
    println("BooleanColumn created")
    assertTrue(n == c.length)
    println(c.get(true).get.size)
    println(c.get(false).get.size)

  }

  @Test def testBigBitSetColumn = {
    val n = N
    import core.collection.immutable.Stream.cons
    def value(i: Int) = math.abs(i % 4) match {
      case 0 => "hello"
      case 1 => "this is the"
      case 2 => "scala"
      case 3 => "world"
    }
    def l(i: Int): Stream[String] = cons(value(nextInt), if (1 < i) l(i - 1) else Stream.empty)
    val c = BitSetColumn[String](l(n))
    println("BitSetColumn created")
    assertTrue(n == c.length)
    println(c.get("hello").get.size)
    println(c.get("scala").get.size)
    println(c.get("world").get.size)
  }

  @Test def testBigFunctionColumn = {
    val n = N
    import core.collection.immutable.Stream.cons
    def l(i: Int): Stream[Int] = cons(nextInt, if (1 < i) l(i - 1) else Stream.empty)
    val c = FunctionColumn[Int](l(n))((i: Int) => i + 1)
    println("FunctionColumn created")
    assertTrue(n == c.length)
    for (i <- 0 until 3) println(c(i))
  }

  @Test def testBigCompressedColumn = {
    val n = N
    import core.collection.immutable.Stream.cons
    def value(i: Int) = (math.abs(i % 3) match {
      case 0 => "hello"
      case 1 => "scala"
      case 2 => "world"
    }) + (nextInt % 200)
    def l(i: Int): Stream[String] = cons(value(nextInt), if (1 < i) l(i - 1) else Stream.empty)
    val c = CompressedColumn[String](l(n))
    println("CompressedColumn created")
    println("CompressedColumn.length " + c.length)
    assertTrue(n == c.length)
    println(c.get("hello1").get.size)
    println(c.get("scala2").get.size)
    println(c.get("world3").get.size)
    for (i <- 0 until 3) println(c(i))
  }

  @Test def testBigMostlyNullColumn = {
    val n = N
    import core.collection.immutable.Stream.cons
    def value(i: Int) = math.abs(i % 3) match {
      case 0 => None
      case 1 => None
      case 2 => Some("world")
    }
    def l(i: Int): Stream[Option[String]] = cons(value(nextInt), if (1 < i) l(i - 1) else Stream.empty)
    val c = MostlyNullColumn[String](l(n))
    println("MostlyNullColumn created")
    println("MostlyNullColumn.length " + c.length)
    assertTrue(n == c.length)
    println(c.get(None).get.size)
    println(c.get(Some("world")).get.size)
    for (i <- 0 until 3) println(c(i))
  }

  @Test def testBigArrayColumn = {
    val n = N
    import core.collection.immutable.Stream.cons
    def l(i: Int): Stream[Double] = cons(nextDouble, if (1 < i) l(i - 1) else Stream.empty)
    val c = ArrayColumn[Double](l(n))
    println("ArrayColumn created")
    println("ArrayColumn.length " + c.length)
    assertTrue(n == c.length)
    for (i <- 0 until 3) println(c(i))
  }

  @Test def testBigUniqueColumn = {
    val n = N
    import core.collection.immutable.Stream.cons
    def l(i: Int): Stream[String] = cons((i * 3).toString, if (1 < i) l(i - 1) else Stream.empty)
    val c = UniqueColumn[String](l(n))
    println("UniqueColumn created")
    println("UniqueColumn.length " + c.length)
    assertTrue(n == c.length)
    for (i <- 0 until 3) println(c(i))
    println("unique('3') " + c.unique("3"))
    println("unique('6') " + c.unique("6"))
    println("unique('9') " + c.unique("9"))
    println("range['3', '9'[ " + c.range("3", "9").size)
    println(c.range("3", "3000"))
  }

  @Test def testBigPrimaryKeyColumn = {
    val n = N
    import core.collection.immutable.Stream.cons
    def l(i: Int): Stream[Double] = cons(((n - i) * 3.0), if (1 < i) l(i - 1) else Stream.empty)
    val c = PrimaryKeyColumn[Double](l(n))
    println("PrimaryKeyColumn created")
    println("PrimaryKeyColumn.length " + c.length)
    assertTrue(n == c.length)
    for (i <- 0 until 3) println(c(i))
    println("unique('3') " + c.unique(3))
    println("unique('6') " + c.unique(6))
    println("unique('9') " + c.unique(9))
  }

  @Test def testBigMemoryPagedPrimaryKeyColumn = {
    for (i <- 1 to 1) {
      val n = N * 1
      import core.collection.immutable.Stream.cons
      def l(i: Int): Stream[Double] = cons(((n - i) * 3.0), if (1 < i) l(i - 1) else Stream.empty)
      val c = MemoryPagedPrimaryKeyColumn[Double](l(n), 4096, 10, 0.3, 4096, 11, 0.53)
      println("MemoryPagedPrimaryKeyColumn created")
      println("MemoryPagedPrimaryKeyColumn " + c.length)
      assertTrue(n == c.length)
      for (i <- 0 until 3) println(c(i))
      println("unique('3') " + c.unique(3))
      println("unique('6') " + c.unique(6))
      println("unique('9') " + c.unique(9))
      for (i <- 30 to 90) println("unique " + (i * 1000) + " " + c.unique(i * 1000))
    }
  }

  @Test def testBigMemoryPagedUniqueColumn = {
    for (i <- 1 to 1) {
      val n = N * 1
      import core.collection.immutable.Stream.cons
      def l(i: Int): Stream[Double] = cons(((n - i) * 3.0), if (1 < i) l(i - 1) else Stream.empty)
      val c = MemoryPagedUniqueColumn[Double](l(n), 4096, 10, 0.3, 4096, 11, 0.53)
      println("MemoryPagedUniqueColumn created")
      println("MemoryPagedUniqueColumn " + c.length)
      assertTrue(n == c.length)
      for (i <- 0 until 3) println(c(i))
      println("unique('3') " + c.unique(3))
      println("unique('6') " + c.unique(6))
      println("unique('9') " + c.unique(9))
      for (i <- 30 to 90) println("unique " + (i * 1000) + " " + c.unique(i * 1000))
    }
  }

  @Test def testBigFilePagedPrimaryKeyColumn = {
    for (i <- 1 to 1) {
      val n = N * 1
      import core.collection.immutable.Stream.cons
      def l(i: Int): Stream[Double] = cons(((n - i) * 3.0), if (1 < i) l(i - 1) else Stream.empty)
      val c = FilePagedPrimaryKeyColumn[Double](l(n), 4096, 10, 0.3, 4096, 14, 0.6)
      println("FilePagedPrimaryKeyColumn created")
      println("FilePagedPrimaryKeyColumn.length " + c.length)
      assertTrue(n == c.length)
      for (i <- 0 until 3) println(c(i))
      println("unique('3') " + c.unique(3))
      println("unique('6') " + c.unique(6))
      println("unique('9') " + c.unique(9))

      println("range " + c.range(210000, 210030))
    }
  }

  @Test def testBigFilePagedUniqueColumn = {
    for (i <- 1 to 1) {
      val n = N * 1
      import core.collection.immutable.Stream.cons
      def l(i: Int): Stream[Double] = cons(((i + 1) * 3.0), if (1 < i) l(i - 1) else Stream.empty)
      val c = FilePagedUniqueColumn[Double](l(n), 4096, 10, 0.3, 4096, 11, 0.53)
      println("FilePagedUniqueColumn created")
      println("FilePagedUniqueColumn " + c.length)
      assertTrue(n == c.length)
      for (i <- 0 until 3) println(c(i))
      println("unique('3') " + c.unique(3))
      println("unique('6') " + c.unique(6))
      println("unique('9') " + c.unique(9))
      for (i <- 30 to 90) println("unique " + (i * 1000) + " " + c.unique(i * 1000))
    }
  }

  @Test def testBigFilePagedUniqueColumnMoreRandom = {
    import core.util.raw._
    import core.util.raw.RawOrdering._
    import core.util.Uuid._
    for (i <- 1 to 1) {
      def randomHex = {
        val h = Array[Char]('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
        val b = new StringBuilder(32)
        for (i <- 0 until 32) b.append(h(math.abs(nextInt) % 16))
        b.toString
      }
      val n = N
      def l(i: Int): Stream[Raw] = cons(Raw(randomHex), if (1 < i) l(i - 1) else Stream.empty)
      val c = infoNanos("create column")(FilePagedUniqueColumn[Raw](l(n), 4096, 24, 0.80, 32 * 1024, 40, 0.64))
      println("FilePagedUniqueColumn created (more random)")
      assertTrue(n == c.length)
      val sortedmap: collection.SortedMap[Raw, Int] = c
      println("FilePagedUniqueColumn " + c.length + ", check ordering")
      var i = 0; var d: Raw = NullRaw; 
      sortedmap.foreach { case (k, _) => try { if (d > k) throw new Exception; d = k; i += 1 } catch { case e => { core.logger.debug("assert failed at " + i + " " + d + " " + k + " " + e) } } }
    }
  }

  @Test def testBigMatrixColumn = {
    val n = N
    import core.collection.immutable.Stream.cons
    import core.math.Matrix
    def m: Matrix = Matrix(
      nextDouble, nextDouble, nextDouble, nextDouble,
      nextDouble, nextDouble, nextDouble, nextDouble,
      nextDouble, nextDouble, nextDouble, nextDouble)
    def l(i: Int): Stream[Matrix] = cons(m, if (1 < i) l(i - 1) else Stream.empty)
    val c = ArrayColumn[Matrix](l(n))
    println("MatrixColumn created")
    println("MatrixColumn.length " + c.length)
    assertTrue(n == c.length)
    for (i <- 0 until 3) println(c(i))
  }

  @Test def testBigFilePagedColumn = {
    for (i <- 1 to 1) {
      val n = N
      import core.collection.immutable.Stream.cons
      import core.math._
      def m(i: Int): Matrix = if (List(0, 1).contains(i % 3))
        Matrix(1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0)
      else
        Matrix(
          nextDouble, nextDouble, nextDouble, nextDouble,
          nextDouble, nextDouble, nextDouble, nextDouble,
          nextDouble, nextDouble, nextDouble, nextDouble)
      def l(i: Int): Stream[Matrix] = cons(m(i), if (1 < i) l(i - 1) else Stream.empty)
      val c = FilePagedColumn(l(n), 4 * 1024, 175, 0.19)
      println("FilePagedColumn created")
      println("FilePagedColumn.length " + c.length)
      assertTrue(n == c.length)
      core.util.time.infoNanos {
        for (i <- 0 until 3) c(i)
        for (i <- 1000 until 1003) c(i)
        for (i <- n - 3 until n) c(i)
      }
    }
  }

  @Test def testBigMemoryPagedColumn = {
    for (i <- 1 to 1) {
      val n = N
      import core.collection.immutable.Stream.cons
      import core.math._
      def m(i: Int): Matrix = if (List(0, 1).contains(i % 3))
        Matrix(1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0)
      else
        Matrix(
          nextDouble, nextDouble, nextDouble, nextDouble,
          nextDouble, nextDouble, nextDouble, nextDouble,
          nextDouble, nextDouble, nextDouble, nextDouble)
      def l(i: Int): Stream[Matrix] = cons(m(i), if (1 < i) l(i - 1) else Stream.empty)
      val c = MemoryPagedColumn(l(n), 4 * 1024, 175, 0.19)
      println("MemoryPagedColumn created")
      println("MemoryPagedColumn.length " + c.length)
      assertTrue(n == c.length)
      core.util.time.infoNanos {
        for (i <- 0 until 3) c(i)
        for (i <- 1000 until 1003) c(i)
        for (i <- n - 3 until n) c(i)
      }
    }
  }

}

