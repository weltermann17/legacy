package com.ibm.haploid

package dx

package engine

package domain

import core.newLogger

import java.io.OutputStream

import javax.xml.bind.{ Marshaller, JAXBContext }

import collection.JavaConversions._

import com.sun.jersey.api.json.{ JSONConfiguration, JSONJAXBContext }

package object marshalling {

  import core.config._

  val formattedoutputxml = getBoolean("haploid.dx.engine.domain.marshalling.formatted-output-xml")

  val formattedoutputjson = getBoolean("haploid.dx.engine.domain.marshalling.formatted-output-json")

  val classeswithjaxbbindings = {
    (getStringList("haploid.dx.engine.domain.marshalling.classes-with-jaxb-bindings").toList ++
      getStringList("haploid.dx.engine.domain.marshalling.custom-classes-with-jaxb-bindings").toList)
      .map(c ⇒ try {
        Class.forName(c)
      } catch {
        case e: Throwable ⇒
          newLogger(this).error("Class not found: " + c)
          throw e
      })
      .toArray
      .asInstanceOf[Array[Class[_]]]
  }

  val jsonconfig = JSONConfiguration
    .natural
    .rootUnwrapping(false)
    .humanReadableFormatting(formattedoutputjson)
    .build

}
