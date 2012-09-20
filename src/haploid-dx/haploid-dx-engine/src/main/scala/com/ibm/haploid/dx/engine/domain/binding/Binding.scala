package com.ibm.haploid

package dx

package engine

package domain

package binding

import java.io.{ OutputStream, ObjectOutputStream, ObjectInputStream, ByteArrayOutputStream, ByteArrayInputStream }
import core.newLogger
import java.math.{ BigDecimal ⇒ JBigDecimal }
import scala.BigDecimal.javaBigDecimal2bigDecimal
import scala.collection.JavaConversions._
import org.apache.commons.codec.binary.Base64
import com.ibm.haploid.core.service.{ Success, Result, Failure }
import com.ibm.haploid.core.util.text._
import com.ibm.haploid.dx.engine.domain.marshalling.Marshaled
import com.ibm.haploid.dx.engine.domain.DomainObject
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl
import akka.actor.ActorPath
import javax.xml.bind.annotation.adapters.{ XmlJavaTypeAdapter, XmlAdapter }
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.MarshalException
import com.ibm.haploid.dx.engine.domain.marshalling.{ Marshal, Unmarshal }
import org.w3c.dom.Node

class BigDecimalAdapter extends XmlAdapter[JBigDecimal, BigDecimal] {
  import BigDecimal.javaBigDecimal2bigDecimal
  def unmarshal(v: JBigDecimal): BigDecimal = v
  def marshal(v: BigDecimal): JBigDecimal = v.underlying
}

class StringOptionAdapter extends OptionAdapter[String]("")

class LongOptionAdapter extends OptionAdapter[java.lang.Long](-1L)

class OptionAdapter[A](nones: A*) extends XmlAdapter[A, Option[A]] {
  def marshal(v: Option[A]): A = v.getOrElse(nones(0))
  def unmarshal(v: A) = if (nones contains v) None else Some(v)
}

class CDataAdapter extends XmlAdapter[String, String] {
  def marshal(v: String): String = "<![CDATA[" + (if (v.contains("\n")) "\n" else "") + v + "]]>"
  def unmarshal(v: String) = fromBase64String(v, "UTF-8")
}

class ActorPathAdapter extends XmlAdapter[String, ActorPath] {
  def marshal(path: ActorPath): String = path.toString
  def unmarshal(s: String): ActorPath = ActorPath.fromString(s)
}

class ResultAdapter extends XmlAdapter[ResultXML, Result[Any]] {
  def marshal(result: Result[Any]): ResultXML = {
    result match {
      case Success(result) ⇒
        ResultXML(true, result)
      case Failure(e) ⇒
        ResultXML(true, ThrowableXML(e))
    }
  }

  def unmarshal(result: ResultXML): Result[_] = {
    result match {
      case ResultXML(true, data) ⇒
        Success(data)
      case ResultXML(false, ThrowableXML(e)) ⇒
        Failure(e)
    }
  }
}

class AnyAdapter extends XmlAdapter[Any, Any] {

  def marshal(any: Any): Any = {
    try {
      TestXML(any).toXml(new OutputStream {
        override def write(b: Int) {}
      })
      any
    } catch {
      case e: MarshalException if allowSerializedXML ⇒
      	if (warnOnSerializedXML) newLogger(this).warning("Need to serialize " + any)
        SerializedXML(any)
      case e: Exception ⇒
        throw (e);
    }
  }

  def unmarshal(any: Any): Any = (if (any.isInstanceOf[ElementNSImpl])
    Unmarshal(any.asInstanceOf[ElementNSImpl])
  else
    any) match {
    case SerializedXML(data) ⇒ data
    case data ⇒ data
  }

}

case class ElementsWrapper[A <: DomainObject](@xmlElementRef elements: JVector[A]) {
  def this() = this(null)
}

class ElementsAdapter[A <: DomainObject] extends XmlAdapter[ElementsWrapper[A], Seq[A]] {
  def marshal(v: Seq[A]) = if (v == null) create(new JVector[A]) else create(new JVector(v))
  def unmarshal(v: ElementsWrapper[A]) = v.elements.toSeq
  def create(v: JVector[A]): ElementsWrapper[A] = ElementsWrapper[A](v)
}

@XmlRootElement(name = "list-element")
case class StringListElement(
  @xmlAttribute(required = true) element: String) {
  def this() = this(null)
}

case class StringListWrapper(@xmlElementRef elements: java.util.Vector[StringListElement]) {
  def this() = this(null)
}

class StringListAdapter extends XmlAdapter[StringListWrapper, Seq[String]] {
  def marshal(v: Seq[String]) = if (v == null)
    StringListWrapper(new java.util.Vector[StringListElement])
  else
    StringListWrapper(new java.util.Vector(v.map(StringListElement(_))))
  def unmarshal(v: StringListWrapper) = v.elements.map(_.element)
}

@XmlRootElement(name = "property")
case class Property(
  @xmlJavaTypeAdapter(classOf[CDataAdapter]) name: String,
  @xmlJavaTypeAdapter(classOf[CDataAdapter]) value: String) {
  def this() = this(null, null)
}

@XmlRootElement(name = "throwable")
class ThrowableXML(

  @xmlElement val throwable: SerializedXML) {

  private def this() = this(null)

}

object ThrowableXML {

  def apply(throwable: Throwable): ThrowableXML = {
    new ThrowableXML(SerializedXML(throwable))
  }

  def unapply(xml: ThrowableXML): Option[Throwable] = try {
    val bis = new ByteArrayInputStream(Base64.decodeBase64(xml.throwable.bytes))
    val ois = new ObjectInputStream(bis);
    Some(ois.readObject.asInstanceOf[Throwable])
  } catch {
    case e: Exception ⇒
      None
  }

}

@XmlRootElement(name = "serialized-object")
class SerializedXML(

  @xmlElement(name = "toString") val string: String,

  @xmlElement val bytes: String) {

  private def this() = this(null, null)

  override def toString = "[SerializedXML] " + string

}

object SerializedXML {

  def apply(obj: Any): SerializedXML = {
    val bos = new ByteArrayOutputStream
    val os = new ObjectOutputStream(bos)
    os.writeObject(obj)
    new SerializedXML(obj.toString, Base64.encodeBase64String(bos.toByteArray))
  }

  def unapply(xml: SerializedXML): Option[Any] = try {
    val bis = new ByteArrayInputStream(Base64.decodeBase64(xml.bytes))
    val ois = new ObjectInputStream(bis);
    Some(ois.readObject)
  } catch {
    case e: Exception ⇒
      None
  }

}

@XmlRootElement(name = "result")
case class ResultXML(

  @xmlAttribute val success: Boolean,

  @xmlJavaTypeAdapter(classOf[AnyAdapter])@xmlAnyElement val data: Any) extends Marshaled {

  @XmlJavaTypeAdapter(classOf[CDataAdapter]) def getText = data.toString

  private def this() = this(true, null)

}

@XmlRootElement(name = "test")
case class TestXML(

  @xmlAnyElement val data: Any) extends Marshaled {

  def this() = this(null)

}

case class PropertiesWrapper(@xmlElementRef properties: java.util.Vector[Property]) {
  def this() = this(null)
}

class PropertiesAdapter extends XmlAdapter[PropertiesWrapper, Map[String, String]] {
  def marshal(v: Map[String, String]): PropertiesWrapper = if (v == null)
    PropertiesWrapper(new java.util.Vector[Property])
  else
    PropertiesWrapper(new java.util.Vector(v.map { case (n, v) ⇒ Property(n, v) }))
  def unmarshal(v: PropertiesWrapper): Map[String, String] =
    v.properties.foldLeft(Map[String, String]()) { case (m, e) ⇒ m ++ Map(e.name -> e.value) }
}