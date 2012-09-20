package com.ibm.haploid

package core

package util

package raw

import java.util.Arrays

class Raw16 private[raw] (private val raw: Array[Byte]) extends Serializable {

  type R = this.type

  def asArray = raw

  override def toString = {
    val s = new StringBuilder(32)
    for (i <- 0 until 16) {
      val index = raw(i)
      s.append(text.hexarray(if (0 > index) 0x100 + index else index))
    }
    s.toString
  }

  override def hashCode = Arrays.hashCode(raw)

  override def equals(other: Any) = try { Arrays.equals(raw, other.asInstanceOf[R].raw) } catch { case _ => false }

}

object Raw16 {

  def apply(hex: String): Raw16 = {
    if (null == hex || 0 == hex.length) {
      NullRaw16
    } else {
      val array = new Array[Byte](16)
      val string = hex.toCharArray
      for (i <- 0 until 16) {
        array.update(i, ((Character.digit(string(2 * i), 16) << 4) + Character.digit(string((2 * i) + 1), 16)).toByte)
      }
      new Raw16(array)
    }
  }

  implicit val Raw16Ordering = new Ordering[Raw16] {

    def compare(x: Raw16, y: Raw16) = x.hashCode.compare(y.hashCode) match {
      case 0 => x.toString.compare(y.toString)
      case c => c
    }

  }

}

object NullRaw16 extends Raw16(Array[Byte](0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)) {

  override def hashCode = Int.MinValue

  override def equals(other: Any) = try { this eq other.asInstanceOf[AnyRef] } catch { case _ => false }

}

