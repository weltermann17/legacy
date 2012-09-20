package com.ibm.haploid

package core

package util

package raw

class Raw8 extends Serializable {

  type R = this.type

  private[raw] def this(raw: Array[Byte]) = {
    this
    this.raw = raw
  }

  def asArray = raw

  def +(other: Raw8) = new Raw16(raw ++ other.raw)

  override def toString = {
    val s = new StringBuilder(16)
    for (i ← 0 until 8) {
      val index = raw(i)
      s.append(text.hexarray(if (0 > index) 0x100 + index else index))
    }
    s.toString
  }

  override def hashCode = java.util.Arrays.hashCode(raw)

  override def equals(other: Any) = try { java.util.Arrays.equals(raw, other.asInstanceOf[R].raw) } catch { case _: Throwable ⇒ false }

  private var raw: Array[Byte] = null

}

object Raw8 {

  def apply(hex: String): Raw8 = {
    if (null == hex || 0 == hex.length) {
      NullRaw8
    } else {
      val array = new Array[Byte](8)
      val string = hex.toCharArray
      for (i ← 0 until 8) {
        array.update(i, ((Character.digit(string(2 * i), 16) << 4) + Character.digit(string((2 * i) + 1), 16)).toByte)
      }
      new Raw8(array)
    }
  }

}

object NullRaw8 extends Raw8(Array[Byte](0, 0, 0, 0, 0, 0, 0, 0))

