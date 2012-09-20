package com.ibm.haploid.io

import java.io.Closeable
import java.io.ByteArrayInputStream
import java.io.OutputStream

sealed protected trait ExternalByteArrayStream extends Closeable {

  protected[this] val array: Array[Byte]

  protected[this] val offset: Int

  protected[this] val length: Int

  protected[this] var position = offset

  protected[this] var lastposition = offset + length

  override def toString = getClass.getName + " a.len " + array.length + ", ofs " + offset + ", len " + length + ", pos " + position + ", lpos" + lastposition

  override def close = lastposition = position
  
}

/**
 * Like java.io.ByteArrayOutputStream, but with an external byte array. For performance reasons it does no testing of array bounds. For this use ExternalByteArrayOutputStreamD.
 */

class ExternalByteArrayOutputStream(
  protected[this] final val array: Array[Byte],
  protected[this] final val offset: Int,
  protected[this] final val length: Int)
  extends OutputStream with ExternalByteArrayStream {

  override def write(i: Int) = {
    array.update(position, i.toByte)
    position += 1
  }

  override def write(a: Array[Byte]) = write(a, 0, a.length)

  override def write(a: Array[Byte], offset: Int, length: Int) = {
    Array.copy(a, offset, array, position, length)
    position += length
  }

  override def flush = {}

  def capacity = lastposition - position

  def getInputStream = new ByteArrayInputStream(array, offset, position - offset)

}

/**
 * Like ExternalByteArrayOutputStream, but does additional bounds tests during initialization and writes.
 */

final class ExternalByteArrayOutputStreamD(
  array: Array[Byte],
  offset: Int,
  length: Int)
  extends ExternalByteArrayOutputStream(array, offset, length) {

  require(offset + length <= array.length)

  override def write(a: Array[Byte], offset: Int, length: Int) = {
    if (position + length > lastposition) throw new IndexOutOfBoundsException("Capacity exceeded. length = " + length + " " + toString)
    super.write(a, offset, length)
  }

}

