package com.ibm.haploid

package core

package util

package io

import java.io.Closeable
import java.io.InputStream

/**
 * Like [[java.io.ByteArrayInputStream]], but with an external bytearray. For performance reasons it does no testing of array bounds. Check available to protect against overflow.
 */

class ByteArrayInputStream(
  private[this] final val array: Array[Byte],
  private[this] final val offset: Int,
  private[this] final val length: Int)
  extends InputStream {

  def this(arr: Array[Byte]) = this(arr, 0, arr.length)

  override def read: Int = {
    val b = array(position)
    position += 1
    b & 0xff
  }

  override def read(a: Array[Byte]) = read(a, 0, a.length)

  override def read(a: Array[Byte], offset: Int, length: Int) = {
    val len = scala.math.min(length, lastposition - position)
    if (0 < len) {
      Array.copy(array, position, a, offset, len)
      position += len
      len
    } else {
      -1
    }
  }

  override def available = lastposition - position

  private[this] var position = offset

  private[this] var lastposition = offset + length

  override def toString = getClass.getName + " a.len " + array.length + ", ofs " + offset + ", len " + length + ", pos " + position + ", lpos" + lastposition

  override def close = lastposition = position

}

