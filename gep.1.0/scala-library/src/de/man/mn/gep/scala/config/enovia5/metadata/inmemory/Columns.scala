package de.man.mn.gep.scala.config.enovia5.metadata.inmemory

import scala.collection.JavaConversions.mapAsScalaMap
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet

abstract sealed class BaseColumn[T] extends Serializable {

  type ColumnType = T

  def set = setIf

  def apply(index: Int, value: T) = set(index, value)

  def afterFill = setIf = (index: Int, value: T) => ()

  val length: Int

  protected var setIf: (Int, T) => Unit

}

trait Unary[T] {

  def get(index: Int): T

  def apply(index: Int) = get(index)

}

trait Column[T] extends BaseColumn[T] with Unary[T]

trait BinaryColumn[T, A] extends BaseColumn[T] with Binary[T, A]

trait Binary[T, A] {

  def get(index: Int)(implicit a: A): T

  def apply(index: Int)(implicit a: A) = get(index)(a)

}

trait Index[T] {

  def lookup(value: T): Set[Int]

}

trait Unique[T] extends Column[T] {

  def unique(value: T): Int // -1 if not found

}

case class BooleanColumn(implicit val length: Int) extends Column[Boolean] with Index[Boolean] {

  def get(index: Int) = trues.contains(index)

  def lookup(value: Boolean): Set[Int] = if (value) trues.toImmutable else falses.toImmutable

  @transient protected var setIf = (index: Int, value: Boolean) => { if (value) trues.add(index) else falses.add(index); () }

  private lazy val trues = new collection.mutable.BitSet(length)

  private lazy val falses = new collection.mutable.BitSet(length)

}

case class BitSetColumn[T](implicit val length: Int) extends Column[T] with Index[T] {

  def get(index: Int): T = {
    bitsets.find { case (v, b) => b.contains(index) } match {
      case Some((value, _)) => value
      case None => throw new IndexOutOfBoundsException(index.toString)
    }
  }

  def lookup(value: T): Set[Int] = bitsets.get(value) match {
    case null => Set[Int]()
    case b => b.toImmutable
  }

  @transient protected var setIf = (index: Int, value: T) => {
    val bitset = bitsets.get(value) match {
      case null => val b = new collection.mutable.BitSet; bitsets.put(value, b); b
      case b => b
    }
    bitset.add(index); ()
  }

  private lazy val bitsets = new java.util.LinkedHashMap[T, collection.mutable.BitSet]

}

case class CompressedColumn[T](implicit val length: Int, m: Manifest[T]) extends Column[T] with Index[T] {

  def get(index: Int): T = distinctvalues(values(index))

  def lookup(value: T): Set[Int] = keys.get(value) match {
    case None => Set[Int]()
    case Some(b) => b
  }

  @transient protected var setIf = (index: Int, value: T) => { keys.put(value, keys.getOrElse(value, Set[Int]()) + index); () }

  @transient private lazy val values = {
    val v = new Array[Int](length)
    var i = 0
    keys.toList.foreach {
      case (value, key) =>
        key.foreach(v.update(_, i))
        i += 1
    }
    v
  }

  @transient private lazy val distinctvalues: Array[T] = {
    val d = new Array[T](keys.size)
    var i = 0
    keys.toList.foreach {
      case (value, key) =>
        d.update(i, value)
        i += 1
    }
    d
  }

  private lazy val keys = new collection.mutable.HashMap[T, Set[Int]]

}

class MostlyNullColumn[@specialized(Int) T](implicit length: Int, m: Manifest[Option[T]]) extends CompressedColumn[Option[T]] {

  private val nulls = new collection.mutable.BitSet

  override def set = setIf2

  override def get(index: Int): Option[T] = if (nulls.contains(index)) None else super.get(index)

  override def lookup(value: Option[T]): Set[Int] = if (value.isEmpty) nulls.toImmutable else super.lookup(value)

  protected var setIf2 = (index: Int, value: Option[T]) => { if (value.isEmpty) nulls.add(index) else setIf(index, value); () }

}

class ArrayColumn[@specialized(Int) T](implicit val length: Int, m: Manifest[T]) extends Column[T] {

  private val values = new Array[T](length)

  def get(index: Int): T = values(index)

  @transient protected var setIf = (index: Int, value: T) => values.update(index, value)

}

class UniqueColumn[@specialized(Int) T](implicit val length: Int, m: Manifest[T]) extends Column[T] with Unique[T] {

  private val values = new Array[T](length)

  def get(index: Int) = values(index)

  def unique(value: T): Int = keys.get(value) match { case None => -1 case Some(index) => index }

  @transient protected var setIf = (index: Int, value: T) => values.update(index, value)

  @transient private lazy val keys = {
    val k = new collection.mutable.HashMap[T, Int]
    var i = 0; values.foreach { v => k.put(v, i); i += 1 }
    k
  }

}

sealed case class Matrix(
  m1: Double,
  m2: Double,
  m3: Double,
  m4: Double,
  m5: Double,
  m6: Double,
  m7: Double,
  m8: Double,
  m9: Double,
  m10: Double,
  m11: Double,
  m12: Double) {

  def print(writer: java.io.PrintWriter) = {
    writer.print("\"m1\":"); trim(m1)
    writer.print(",\"m2\":"); trim(m2)
    writer.print(",\"m3\":"); trim(m3)
    writer.print(",\"m4\":"); trim(m4)
    writer.print(",\"m5\":"); trim(m5)
    writer.print(",\"m6\":"); trim(m6)
    writer.print(",\"m7\":"); trim(m7)
    writer.print(",\"m8\":"); trim(m8)
    writer.print(",\"m9\":"); trim(m9)
    writer.print(",\"m10\":"); trim(m10)
    writer.print(",\"m11\":"); trim(m11)
    writer.print(",\"m12\":"); trim(m12)

    def trim(d: Double): Unit = if (0 == d) writer.print(0) else if (1 == d) writer.print(1) else if (-1 == d) writer.print(-1) else writer.print(d)
  }

  def toMap = Map(
    "m1" -> m1,
    "m2" -> m2,
    "m3" -> m3,
    "m4" -> m4,
    "m5" -> m5,
    "m6" -> m6,
    "m7" -> m7,
    "m8" -> m8,
    "m9" -> m9,
    "m10" -> m10,
    "m11" -> m11,
    "m12" -> m12)

}

object UnityMatrix extends Matrix(1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0) {

  override def print(writer: java.io.PrintWriter) = {
    writer.print(this)
  }

  override def toMap = Map(
    "m1" -> 1,
    "m2" -> 0,
    "m3" -> 0,
    "m4" -> 0,
    "m5" -> 1,
    "m6" -> 0,
    "m7" -> 0,
    "m8" -> 0,
    "m9" -> 1,
    "m10" -> 0,
    "m11" -> 0,
    "m12" -> 0)

  override def toString = """"m1":1,"m2":0,"m3":0,"m4":0,"m5":1,"m6":0,"m7":0,"m8":0,"m9":1,"m10":0,"m11":0,"m12":0"""

}

object Matrix {

  def apply(result: RichResultSet) = {
    new Matrix(e(result), e(result), e(result), e(result), e(result), e(result), e(result), e(result), e(result), e(result), e(result), e(result)) match {
      case UnityMatrix => UnityMatrix
      case m => m
    }
  }

  private def e(d: Double) = {
    val epsilon = 1e-15
    if (epsilon > math.abs(d)) 0 else if (epsilon > math.abs(1 - d)) 1 else if (epsilon > math.abs(-1 - d)) -1 else d
  }

}

class MatrixColumn(implicit val length: Int) extends Column[Matrix] {

  def get(index: Int) = try { values(index) } catch { case _ => UnityMatrix }

  @transient protected var setIf = (index: Int, value: Matrix) => values.update(index, value)

  private lazy val values = new Array[Matrix](length)

}

abstract class FunctionColumn[T](implicit val length: Int) extends Column[T] {

  @transient val f: Int => T

  def get(index: Int) = f(index)

  @transient protected var setIf = (index: Int, value: T) => ()

}

abstract class FunctionColumnA[T, A](implicit val length: Int) extends BinaryColumn[T, A] {

  @transient val f: Int => A => T

  def get(index: Int)(implicit a: A) = f(index)(a)

  @transient protected var setIf = (index: Int, value: T) => ()

}

abstract class ReverseLookupColumn[T](implicit val length: Int) extends Column[T] with Unique[T] {

  @transient var f: Int => T = null

  @transient var f_inverse: T => Int = null

  def get(index: Int) = f(index)

  def unique(value: T): Int = f_inverse(value)

  @transient protected var setIf = (index: Int, value: T) => {
    f = (index: Int) => value
    f_inverse = (value: T) => index
  }

}

