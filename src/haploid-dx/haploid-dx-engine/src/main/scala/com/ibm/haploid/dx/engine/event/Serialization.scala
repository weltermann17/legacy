package com.ibm.haploid

package dx

package engine

package event

import java.io.{ ObjectOutputStream, ObjectInputStream, ByteArrayOutputStream, ByteArrayInputStream }
import java.util.zip.{ GZIPInputStream, GZIPOutputStream, Deflater }

object Serialization {

  def serialize(any: AnyRef) = {
    val bos = new ByteArrayOutputStream(1024)
    val zip = if (usecompressionduringserialization)
      new GZIPOutputStream(bos, 1024) {
        `def`.setLevel(Deflater.BEST_SPEED)
      }
    else
      bos
    val out = new ObjectOutputStream(zip)
    out.writeObject(any)
    out.close
    bos.toByteArray
  }

  def deserialize[T](array: Array[Byte]): T = {
    val bis = new ByteArrayInputStream(array)
    val zip = new GZIPInputStream(bis)
    val in = new ObjectInputStream(zip)
    in.readObject.asInstanceOf[T]
  }

}

