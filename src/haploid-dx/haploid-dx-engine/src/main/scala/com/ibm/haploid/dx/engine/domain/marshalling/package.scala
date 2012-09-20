package com.ibm.haploid

package dx

package engine

package domain

import java.io.OutputStream

import javax.xml.bind.{ Marshaller, JAXBContext }

import collection.JavaConversions._

import com.sun.jersey.api.json.{ JSONConfiguration, JSONJAXBContext }

package object marshalling {

  import core.config._

  val formattedoutputxml = getBoolean("haploid.dx.engine.domain.marshalling.formatted-output-xml")

  val formattedoutputjson = getBoolean("haploid.dx.engine.domain.marshalling.formatted-output-json")

  val classeswithjaxbbindings = try {
    (getStringList("haploid.dx.engine.domain.marshalling.classes-with-jaxb-bindings").toList ++
      getStringList("haploid.dx.engine.domain.marshalling.custom-classes-with-jaxb-bindings").toList)
      .map(c => Class.forName(c))
      .toArray
      .asInstanceOf[Array[Class[_]]]
  } catch {
    case e => 
      e.printStackTrace
      throw e
  }

  val jsonconfig = JSONConfiguration
    .natural
    .rootUnwrapping(false)
    .humanReadableFormatting(formattedoutputjson)
    .build

}
