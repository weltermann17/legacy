package com.ibm.haploid

package core

import java.lang.reflect.{ Modifier, Method }
import scala.util.matching.Regex

/**
 * Some tools to ease the use the Java reflection api in Scala.
 */
package object reflect {

  /**
   * Returns the primitive corresponding to it, for example Int for java.lang.Integer
   */
  def primitive(clazz: Class[_]) = clazz.getName match {
    case "java.lang.Boolean" => classOf[Boolean]
    case "java.lang.Byte" => classOf[Byte]
    case "java.lang.Character" => classOf[Char]
    case "java.lang.Short" => classOf[Short]
    case "java.lang.Integer" => classOf[Int]
    case "java.lang.Long" => classOf[Long]
    case "java.lang.Float" => classOf[Float]
    case "java.lang.Double" => classOf[Double]
    case _ => clazz
  }

  /**
   * Returns the boxed corresponding to it, for example java.lang.Integer for Int
   */
  def boxed(clazz: Class[_]) = clazz.getName match {
    case "boolean" => classOf[java.lang.Boolean]
    case "byte" => classOf[java.lang.Byte]
    case "char" => classOf[java.lang.Character]
    case "short" => classOf[java.lang.Short]
    case "int" => classOf[java.lang.Integer]
    case "long" => classOf[java.lang.Long]
    case "float" => classOf[java.lang.Float]
    case "double" => classOf[java.lang.Double]
    case _ => clazz
  }
  
  /**
   * Returns the boxed value for the given primitive value: simply call p.asInstanceOf[AnyRef]
   */

  /**
   * As a property we define all public val members of an object of type T, we return a map of their names and values.
   */
  def properties[T](any: AnyRef)(implicit manifest: Manifest[T]): Map[String, T] = {
    propertyNames(any)
      .map { m => (m.getName, m.invoke(any)) }
      .filter { case (_, v) => manifest.erasure.isInstance(v) }
      .map { e => (e._1, e._2.asInstanceOf[T]) }
      .toMap
  }

  def propertyNames(any: AnyRef) = any.getClass.getMethods.filter(isProperty(_)).toList.sortWith((a, b) => a.getName < b.getName)

  private def isProperty(m: Method) = {
    lazy val name = m.getName
    lazy val returntype = m.getReturnType.getName
    (0 != (m.getModifiers & Modifier.PUBLIC)) &&
      ("[B" != returntype) &&
      (!returntype.contains("java")) &&
      (!returntype.contains("scala")) &&
      (!m.getReturnType.isPrimitive) &&
      (0 == m.getParameterTypes.length) &&
      (!name.startsWith("_")) &&
      (!name.endsWith("$")) &&
      (!(base.exists(_.findFirstIn(name) != None)))
  }

  private val base = List(
    """^toString$""".r,
    """\$""".r,
    """^toMap""".r,
    """^curried""".r,
    """^curry""".r,
    """^isPersisted""".r,
    """^tupled""".r,
    """^copy""".r,
    """^columns""".r,
    """^filled""".r,
    """^size""".r,
    """^copyFrom""".r,
    """^outer""".r,
    """^productArity$""".r,
    """^productIterator$""".r,
    """^productElements$""".r,
    """^productPrefix$""".r,
    """^hashCode$""".r,
    """^get""".r,
    """^readResolve$""".r)

}
