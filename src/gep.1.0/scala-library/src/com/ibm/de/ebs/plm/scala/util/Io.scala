package com.ibm.de.ebs.plm.scala.util

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import org.restlet.data.Disposition
import org.restlet.representation.Representation

import com.ibm.de.ebs.plm.scala.rest.Services
import com.ibm.de.ebs.plm.scala.text.StringConversions.fromHexString

object Io {

  object nullstream extends java.io.ByteArrayOutputStream {

    override def size = 0

    override def toString = ""

    override def toString(charset: String) = toString

    override def toByteArray: Array[Byte] = nullarray

    override def writeTo(out: java.io.OutputStream) = {}

    override def write(i: Int) = {}

    override def write(a: Array[Byte]) = {}

    override def write(a: Array[Byte], offset: Int, length: Int) = {}

    override def close = {}

    override def flush = {}

    override def reset = {}

    private val nullarray = new Array[Byte](0)

  }

  def addZipEntry(name: String)(p: => Unit)(implicit z: ZipOutputStream) = {
    try {
      z.putNextEntry(new ZipEntry(name))
      p
    } catch {
      case e => println(e)
    } finally {
      z.closeEntry
    }
  }

  def copyBytes(in: InputStream, out: OutputStream) {
    val bufferedin = new BufferedInputStream(in, buffersize)
    var bytesread = 0
    val buffer = new Array[Byte](buffersize)
    while (-1 < { bytesread = bufferedin.read(buffer, 0, buffersize); bytesread }) {
      out.write(buffer, 0, bytesread)
    }
    out.flush
  }

  def copyText(in: Reader, out: Writer) {
    val bufferedin = new BufferedReader(in, buffersize)
    var bytesread = 0
    val buffer = new Array[Char](buffersize)
    while (-1 < { bytesread = bufferedin.read(buffer, 0, buffersize); bytesread }) {
      out.write(buffer, 0, bytesread)
    }
    out.flush
  }

  def addDisposition(representation: Representation, parameters: Map[String, String], format: String) = {
    if (null != representation) {
      val readablenicename = fromHexString(parameters("nicename"))
      val mediatype = Services.Metadata.getMediaType(parameters(format))
      val disposition = new Disposition(Disposition.TYPE_ATTACHMENT)
      disposition.setFilename(readablenicename)
      disposition.setSize(representation.getSize)
      representation.setDisposition(disposition)
      representation.setMediaType(mediatype)
    }
    representation
  }

  val buffersize = 51 * 1024

}