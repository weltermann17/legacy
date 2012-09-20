package com.ibm.de.ebs.plm.scala.database

import java.lang.reflect.Method
import java.lang.reflect.Modifier

trait PropertiesMapper {

  def toMap: Map[String, Any] = {
    PropertiesMapper.getProperties(this).map { m =>
      val name = m.getName
      val value = m.invoke(this) match {
        case Some(v) => v
        case None | null => null
        case v => v
      }
      (name, value)
    }.filter(null != _._2).toMap
  }

  def getPropertiesNames: List[String] = {
    PropertiesMapper.getProperties(this).map { m =>
      m.getName
    }.sorted.toList
  }

  override def toString = com.ibm.de.ebs.plm.scala.json.Json.build(toMap)

}

object PropertiesMapper {

  private def getProperties(any: AnyRef) = {
    val key = any.getClass.getName
    if (propertiesmap.containsKey(key)) {
      propertiesmap.get(key)
    } else {
      val properties = any.getClass.getMethods.filter(isProperty _)
      propertiesmap.put(key, properties)
      properties
    }
  }

  private val ignorelist = List(
    """^toString$""".r,
    """\$""".r,
    """^toMap""".r,
    """^curried""".r,
    """^curry""".r,
    """^isPersisted""".r,
    """^tupled""".r,
    """^copy""".r,
    """^outer""".r,
    """^productArity$""".r,
    """^productIterator$""".r,
    """^productElements$""".r,
    """^productPrefix$""".r,
    """^hashCode$""".r,
    """^get""".r,
    """^readResolve$""".r)

  private def skip(name: String) = PropertiesMapper.ignorelist.exists(_.findFirstIn(name) != None)

  private def isProperty(m: Method) = {
    (0 != (m.getModifiers & Modifier.PUBLIC)) &&
      ("void" != m.getReturnType.getName) &&
      ("[B" != m.getReturnType.getName) &&
      (0 == m.getParameterTypes.length) &&
      (!m.getName.startsWith("_")) &&
      (!m.getName.endsWith("$")) &&
      (!skip(m.getName))
  }

  private val propertiesmap = new java.util.concurrent.ConcurrentHashMap[String, Array[Method]]

}
