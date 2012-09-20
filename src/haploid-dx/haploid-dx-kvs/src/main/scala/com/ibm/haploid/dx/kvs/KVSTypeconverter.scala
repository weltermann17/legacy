package com.ibm.haploid

package dx

package kvs

import org.restlet.Response

import com.ibm.haploid.core.newLogger
import com.typesafe.config.{ ConfigFactory, Config }

trait KVSTypeconverter {

  val logger = newLogger(this)

  implicit def kvsObjToOption(obj: KVSObject): Option[KVSObject] = Some(obj)

  implicit def tupleToMap(tuple: (String, String)): Map[String, String] = Map(tuple._1 -> tuple._2)

  implicit def fromStringToConfig(in: String): Config = {
    val s = in.substring(in.indexOf("#"))
    val begin = s.trim.stripMargin.split("\n")(0)

    if (begin.startsWith("##ERROR")) {
      val config = s.replace("##ERROR", "ERROR {").replace("##END", "}").lines.foldLeft(new String) { (s1, s2) =>
        if (s2.trim == "") {
          s1
        } else if (s2.contains("=") && !s2.contains("{")) {
          s1 + s2.replaceFirst("=", "=\"") + "\"\n"
        } else {
          s1 + s2 + "\n"
        }
      }

      return ConfigFactory.parseString(config)
    } else if (begin.startsWith("##BEGIN")) {
      val typ = {
        if (begin.length >= 8)
          begin.substring(8)
        else
          "data"
      }

      val separator = "##END %1\n##BEGIN %1".replace("%1", typ)

      val config = s.replace(separator, "}, {").lines.foldLeft(new String) { (s1, s2) =>
        if (s2.contains("##BEGIN")) {
          s1 + typ.toUpperCase + " = [{\n"
        } else if (s2.contains("##END")) {
          s1 + "}]"
        } else if (s2.contains("=")) {
          s1.reverse.replaceFirst("\n", "\"\n").reverse + s2.replaceFirst("=", "\"=\"") + "\"\n"
        } else {
          s1 + s2 + "\n"
        }
      }
      ConfigFactory.parseString(config)
    } else {
      throw new IllegalArgumentException("String can't be parsed.")
    }
  }

  implicit def responseToString(response: Response): String = {
    val s = response.getEntity().getText()
    logger.debug(s)
    s
  }

}