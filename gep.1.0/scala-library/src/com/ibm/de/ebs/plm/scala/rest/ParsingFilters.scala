package com.ibm.de.ebs.plm.scala.rest

import java.io.Reader
import java.io.StringWriter
import java.io.Writer
import java.nio.channels.Channels
import java.nio.charset.Charset

import scala.util.parsing.combinator.RegexParsers

import org.restlet.data.Language
import org.restlet.representation.Representation
import org.restlet.representation.StringRepresentation
import org.restlet.routing.Filter
import org.restlet.Context
import org.restlet.Request
import org.restlet.Response
import org.restlet.Restlet

import com.ibm.de.ebs.plm.scala.parsing.combinator.ProcessResult
import com.ibm.de.ebs.plm.scala.resource.b
import com.ibm.de.ebs.plm.scala.resource.d
import com.ibm.de.ebs.plm.scala.resource.disposable
import com.ibm.de.ebs.plm.scala.resource.forceContextType
import com.ibm.de.ebs.plm.scala.resource.using
import com.ibm.de.ebs.plm.scala.util.Io.buffersize
import com.ibm.de.ebs.plm.scala.util.Timers.time

trait FilterParser extends RegexParsers with ProcessResult[Unit, Unit] {

  def parse(reader: Reader): Unit

  def parse(reader: Reader, writer: Writer): Long = {
    this.writer = writer
    parse(reader)
    len
  }

  protected def write(s: String) = {
    len += s.length
    writer.write(s)
  }

  protected def getWriter = writer

  private var writer: Writer = null
  private var len: Long = 0
}

case class ParsingFilter(parser: FilterParser, next: Restlet, context: Context)
  extends Filter(context, next) {

  override protected def afterHandle(request: Request, response: Response) = {
    if (null != parser) {
      response.setEntity(parseRepresentation(response.getEntity))
    }
    super.afterHandle(request, response)
  }

  private def parseRepresentation(representation: Representation): Representation = {
    val text = using {
      implicit val _ = forceContextType[String]
      val rep = disposable(representation)
      val decoder = Charset.forName("UTF-8").newDecoder
      val encoder = Charset.forName("UTF-8").newEncoder
      val reader = disposable(Channels.newReader(rep.getChannel, decoder, buffersize))
      val writer = disposable(new StringWriter(buffersize))
      parser.parse(reader, writer)
      writer.flush
      writer.toString
    }
    new StringRepresentation(text, representation.getMediaType, Language.ALL, representation.getCharacterSet)
  }

}

/**
 * Example: Convert all lowercase l into uppercase L
 */

case class SampleParser(b: Boolean) extends FilterParser {
  override def parse(reader: Reader) = {
    println(time(processShort(parseAll(sample(getWriter), reader)) { _ => () }))
  }
  def sample(w: Writer): Parser[Unit] = rep(any(w) | l(w)) ^^ { _ => w.flush }
  def any(w: Writer): Parser[Unit] = """^(?:(?!\:\:)[^l])""".r ^^ { c => w.write(c) }
  def l(w: Writer): Parser[Unit] = "l".r ^^ { _ => w.write("L") }
}
