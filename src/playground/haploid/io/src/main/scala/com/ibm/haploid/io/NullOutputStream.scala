package com.ibm.haploid.io

import java.io.OutputStream

/**
 * Writing to the NullOutputStream object is like writing to /dev/null. 
 * It is thread-safe. Use it if you need to write to an OutputStream, but have no interest in the output.
 */
object NullOutputStream extends OutputStream {

  override def toString = ""

  override def write(i: Int) = {}

  override def write(a: Array[Byte]) = {}

  override def write(a: Array[Byte], offset: Int, length: Int) = {}

  override def close = {}

  override def flush = {}

}

