package com.ibm.haploid

package dx

package engine

package domain

package marshalling

import java.io.OutputStream
import java.io.Writer
import javax.xml.bind.{ Marshaller, JAXBContext }
import com.sun.xml.bind.marshaller.NioEscapeHandler
import com.sun.jersey.api.json.{ JSONConfiguration, JSONJAXBContext }
import com.ibm.haploid.dx.engine.domain.binding.ActorPathAdapter

trait Marshaled {

  def toXml(out: OutputStream) = {
    Marshal(this, out)
  }

  def toJson(out: OutputStream) = {
    val jsoncontext = new JSONJAXBContext(jsonconfig, classeswithjaxbbindings: _*)
    val marshaller = jsoncontext.createJSONMarshaller
    marshaller.marshallToJSON(this, out)
  }

}

object Marshal {

  def apply(any: Any, out: OutputStream) {
    val xmlcontext = JAXBContext.newInstance(classeswithjaxbbindings: _*)
    val marshaller = xmlcontext.createMarshaller
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedoutputxml)
    marshaller.setProperty("com.sun.xml.bind.characterEscapeHandler", CDataEscapeHandler)
    marshaller.marshal(any, out)
  }

}

object CDataEscapeHandler

  extends NioEscapeHandler("UTF-8") {

  override def escape(chars: Array[Char], start: Int, length: Int, isattributevalue: Boolean, out: Writer) = {
    if (chars.startsWith("<![CDATA[")) out.write(chars) else super.escape(chars, start, length, isattributevalue, out)
  }

}

