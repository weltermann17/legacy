package com.ibm.haploid

import java.io.{ InputStream, OutputStream, BufferedInputStream, Reader, Writer, BufferedReader, ByteArrayOutputStream }

package object io {
  
  def getConfiguration: String = {
    val r = getClass.getResourceAsStream("/com/ibm/haploid/io/reference.conf")
    println("r as stream" + r)
    import java.io._
    val reader = new LineNumberReader(new InputStreamReader(r))
    reader.readLine
  }

  /**
   * Copies the entire inputstream to outputstream, then flushs the outputstream.
   */
  def copyBytes(in: InputStream, out: OutputStream, buffersize: Int = defaultbuffersize) {
    val bufferedin = new BufferedInputStream(in, buffersize)
    var bytesread = 0
    val buffer = new Array[Byte](buffersize)
    while (-1 < { bytesread = bufferedin.read(buffer, 0, buffersize); bytesread }) {
      out.write(buffer, 0, bytesread)
    }
    out.flush
  }

  /**
   * Copies the entire reader to writer, then flushs the writer.
   */
  def copyText(in: Reader, out: Writer, buffersize: Int = defaultbuffersize) {
    val bufferedin = new BufferedReader(in, buffersize)
    var bytesread = 0
    val buffer = new Array[Char](buffersize)
    while (-1 < { bytesread = bufferedin.read(buffer, 0, buffersize); bytesread }) {
      out.write(buffer, 0, bytesread)
    }
    out.flush
  }

  /**
   * Copies the entire inputstream to a ByteArrayOutputStream
   */
  def copyFully(in: InputStream, buffersize: Int = defaultbuffersize) = {
    val out = new ByteArrayOutputStream
    copyBytes(in, out)
    out
  }

  /**
   * Sets the default buffersize used internally. Not thread-safe, therefore, use from a single thread only (eg. at startup).
   */
  def setDefaultBuffersSize(buffersize: Int) = {
    defaultbuffersize = buffersize
  }

  /**
   * If not set differently this will result to 51k which proved to provide good thruput in intranet environments.
   */
  def getDefaultBufferSize = defaultbuffersize

  private[this] var defaultbuffersize = 51 * 1024

}