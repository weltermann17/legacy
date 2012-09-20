package com.ibm.haploid

package core

package util

import java.io.{ Writer, Reader, OutputStream, InputStream }
import java.nio.channels.Channels.newChannel
import java.nio.channels.{ WritableByteChannel, ReadableByteChannel, FileChannel }
import java.nio.ByteBuffer

package object io {

  import core.config._

  val buffersize = getBytes("haploid.core.util.io.buffersize").toInt

  /**
   * Copies the entire inputstream to outputstream, then flushs the outputstream.
   */
  def copyBytes(in: InputStream, out: OutputStream, buffersize: Int = getDefaultBufferSize) {
    copyBytesNio(newChannel(in), newChannel(out), buffersize)
    out.flush
  }

  /**
   * Copies the entire inputstream to outputstream, then flushs the outputstream.
   */
  def copyBytesIo(in: InputStream, out: OutputStream, buffersize: Int = getDefaultBufferSize) {
    val buffer = new Array[Byte](buffersize)
    var bytesread = 0
    while (-1 < { bytesread = in.read(buffer, 0, buffersize); bytesread }) {
      out.write(buffer, 0, bytesread)
    }
    out.flush
  }

  /**
   * Copies the entire readable channel to the writable channel.
   */
  def copyBytesNio(in: ReadableByteChannel, out: WritableByteChannel, buffersize: Int = getDefaultBufferSize) {
    if (in.isInstanceOf[FileChannel]) {
      val f = in.asInstanceOf[FileChannel]
      f.transferTo(0, f.size, out)
    } else if (out.isInstanceOf[FileChannel]) {
      val f = out.asInstanceOf[FileChannel]
      f.transferFrom(in, 0, Long.MaxValue)
    } else {
      val b = ByteBuffer.allocateDirect(buffersize)
      var bytesread = 0
      while (-1 < { bytesread = in.read(b); bytesread }) {
        b.flip
        out.write(b)
        b.clear
      }
    }
  }

  /**
   * Copies the entire reader to writer, then flushs the writer.
   */
  def copyText(in: Reader, out: Writer, buffersize: Int = defaultbuffersize) {
    var bytesread = 0
    val buffer = new Array[Char](buffersize)
    while (-1 < { bytesread = in.read(buffer, 0, buffersize); bytesread }) {
      out.write(buffer, 0, bytesread)
    }
    out.flush
  }

  /**
   * Copies the entire reader to writer line by line to fix newline problems, then flushs the writer.
   */
  def copyLines(in: Reader, out: Writer, buffersize: Int = defaultbuffersize) {
    val reader = new java.io.LineNumberReader(in)
    val writer = new java.io.PrintWriter(out)
    var line: String = null
    while (null != { line = reader.readLine; line }) {
      writer.println(line)
    }
    writer.flush
  }

  /**
   * Copies the entire inputstream to a ByteArrayOutputStream
   */
  def copyFully(in: InputStream, buffersize: Int = defaultbuffersize) = {
    val out = new java.io.ByteArrayOutputStream
    copyBytes(in, out)
    out
  }

  /**
   * Sets the default buffersize used internally. Not thread-safe, should only be set at configuration time.
   */
  def setDefaultBuffersSize(buffersize: Int) = {
    defaultbuffersize = buffersize
  }

  /**
   * If not set differently this will result to 2k which proved to provide best performance under high load.
   */
  def getDefaultBufferSize = defaultbuffersize

  /**
   * after a lot of tests this seems to be the most reasonable fast but still small buffer size
   */
  private[this] var defaultbuffersize = 2 * 1024

}
