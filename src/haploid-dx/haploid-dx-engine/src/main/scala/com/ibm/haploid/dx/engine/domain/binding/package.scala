package com.ibm.haploid

package dx

package engine

package domain

import javax.xml.bind.annotation.adapters._
import javax.xml.bind.annotation._
import scala.annotation.target.field

package object binding {

  type xmlAnyElement = XmlAnyElement @field
  type xmlAttribute = XmlAttribute @field
  type xmlElement = XmlElement @field
  type xmlElementRef = XmlElementRef @field
  type xmlElementRefs = XmlElementRefs @field
  type xmlElementWrapper = XmlElementWrapper @field
  type xmlJavaTypeAdapter = XmlJavaTypeAdapter @field
  type xmlTransient = XmlTransient @field

  type JVector[A <: DomainObject] = java.util.Vector[A]

}
