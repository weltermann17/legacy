package com.ibm.de.ebs.plm.scala.database

class Raw extends Serializable {

  private[database] def this(raw: Array[Byte]) = {
    this
    this.raw = raw
  }

  override def toString = {
    val s = new StringBuilder(32)
    val hexArray = com.ibm.de.ebs.plm.scala.text.StringConversions.hexArray
    for (i <- 0 until 16) {
      val index = raw(i)
      s.append(hexArray(if (0 > index) 0x100 + index else index))
    }
    s.toString
  }

  override def hashCode = java.util.Arrays.hashCode(raw)

  override def equals(other: Any) = null != other && java.util.Arrays.equals(raw, other.asInstanceOf[Raw].raw)

  private var raw: Array[Byte] = null

}

object Raw {

  def apply(hex: String): Raw = {
    if (null == hex || 0 == hex.length) {
      NullRaw
    } else {
      val array = new Array[Byte](16)
      val string = hex.toCharArray
      for (i <- 0 until 16) {
        array.update(i, ((Character.digit(string(2 * i), 16) << 4) + Character.digit(string((2 * i) + 1), 16)).toByte)
      }
      new Raw(array)
    }
  }

}

object NullRaw extends Raw(Array[Byte](0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
