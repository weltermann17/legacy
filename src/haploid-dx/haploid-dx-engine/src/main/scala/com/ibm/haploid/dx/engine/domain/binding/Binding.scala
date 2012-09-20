package com.ibm.haploid

package dx

package engine

package domain

package binding

import java.math.{ BigDecimal => JBigDecimal }

import javax.xml.bind.annotation.adapters.XmlAdapter

import scala.BigDecimal.javaBigDecimal2bigDecimal
import scala.collection.JavaConversions.{ seqAsJavaList, asScalaBuffer }

import com.ibm.haploid.dx.engine.domain.DomainObject

class BigDecimalAdapter extends XmlAdapter[JBigDecimal, BigDecimal] {
  import BigDecimal.javaBigDecimal2bigDecimal
  def unmarshal(v: JBigDecimal): BigDecimal = v
  def marshal(v: BigDecimal): JBigDecimal = v.underlying
}

class StringOptionAdapter extends OptionAdapter[String](null, "")

class OptionAdapter[A](nones: A*) extends XmlAdapter[A, Option[A]] {
  def marshal(v: Option[A]): A = v.getOrElse(nones(0))
  def unmarshal(v: A) = if (nones contains v) None else Some(v)
}

class CDataAdapter extends XmlAdapter[String, String] {
  def marshal(v: String): String = "<![CDATA[" + (if (v.contains("\n")) "\n" else "") + v + "]]>"
  def unmarshal(v: String) = v
}

case class ElementsWrapper[A <: DomainObject](@xmlElementRef elements: JVector[A]) {
  def this() = this(null)
}

class ElementsAdapter[A <: DomainObject] extends XmlAdapter[ElementsWrapper[A], Seq[A]] {

  def marshal(v: Seq[A]) = if (v == null) create(new JVector[A]) else create(new JVector(v))
  def unmarshal(v: ElementsWrapper[A]) = v.elements.toSeq
  def create(v: JVector[A]): ElementsWrapper[A] = new ElementsWrapper[A](v)
}

