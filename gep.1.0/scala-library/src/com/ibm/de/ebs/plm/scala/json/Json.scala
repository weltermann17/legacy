package com.ibm.de.ebs.plm.scala.json

/*
 * Copyright 2009 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Modifications:
 * added a case class Json to be returned from parsing to ease access to its members
 * added implicit conversions from/to Json, only active if explicitly imported 
 */

import java.io.Reader

import scala.collection.immutable.Seq
import scala.collection.immutable.List
import scala.collection.Map
import scala.util.parsing.combinator.JavaTokenParsers

import org.codehaus.jackson.map.ObjectMapper

import collection.JavaConversions._

import com.ibm.de.ebs.plm.scala.json.helpers.stringToConfiggyString

private final class JsonParser extends JavaTokenParsers {
  def obj: Parser[Json] = "{" ~> repsep(member, ",") <~ "}" ^^ { m => Json(Map[String, Json]() ++ m) }
  def arr: Parser[Json] = "[" ~> repsep(value, ",") <~ "]" ^^ { l => Json(List[Json]() ++ l) }
  def member: Parser[(String, Json)] = string ~ ":" ~ value ^^ { case name ~ ":" ~ v => (name.asString, v) }
  def number: Parser[Json] = floatingPointNumber ^^ {
    case num if num.matches(".*[.eE].*") => {
      val n = BigDecimal(num)
      Json(if (n >= scala.Double.MinValue && n <= scala.Double.MaxValue) n.toDouble else n)
    }
    case num => {
      val n = num.toLong
      Json(if (n >= scala.Int.MinValue && n <= scala.Int.MaxValue) n.toInt else n)
    }
  }
  def string: Parser[Json] = {
    "\"" ~> """([^\"[\x00-\x1F]\\]+|\\[\\/bfnrt"]|\\u[a-fA-F0-9]{4})*""".r <~ "\"" ^^
      { s => Json(s.replace("""\/""", "/").unquoteC) }
  }
  def value: Parser[Json] =
    obj | arr | string | number | "true" ^^^ Json(true) | "false" ^^^ Json(false) | "null" ^^^ Json(null)
  def parse(s: String): Json = {
    parseAll(value, s) match {
      case Success(result, _) => result
      case e @ Failure(msg, z) => throw new JsonException(e.toString)
      case e @ Error(msg, _) => throw new JsonException(e.toString)
    }
  }
  def parse(reader: java.io.Reader): Json = {
    parseAll(value, reader) match {
      case Success(result, _) => result
      case e @ Failure(msg, z) => throw new JsonException(e.toString)
      case e @ Error(msg, _) => throw new JsonException(e.toString)
    }
  }
}

private final case class QuotedString(inner: String) {
  override def toString = inner
}

sealed case class Json(any: Any) {
  override def toString = any match { case Some(json: Json) => json.toString case null => "null" case _ => any.toString }
  def asNull = convert[Null](null)
  def asBoolean: Boolean = any match { case Some(json: Json) => json.asBoolean case b: Boolean => b case s: String => s.toBoolean case _ => convert[Boolean](false) }
  def asInt: Int = any match { case Some(json: Json) => json.asInt case i: Int => i case l: Long => l.toInt case d: Double => d.toInt case s: String => s.toInt case _ => convert[Int](0) }
  def asLong: Long = any match { case Some(json: Json) => json.asLong case i: Int => i.toLong case s: String => s.toLong case _ => convert[Long](0) }
  def asDouble: Double = any match { case Some(json: Json) => json.asDouble case i: Int => i.toDouble case l: Long => l.toDouble case s: String => s.toDouble case _ => convert[Double](0.) }
  def asBigDecimal: BigDecimal = any match { case Some(json: Json) => json.asBigDecimal case _ => convert[BigDecimal](0.) }
  def asString = convert[String]("")
  def asArray = convert[Json.JArray](List[Json]())
  def asObject = convert[Json.JObject](Map[String, Json]())
  private def convert[T](default: T): T = try {
    any match {
      case Some(json: Json) => json.convert[T](default)
      case None => default
      case null => null.asInstanceOf[T]
      case a: Any => a.asInstanceOf[T]
    }
  } catch { case e => throw new JsonException("Conversion failed: " + e.getMessage) }
}

class JsonException(reason: String) extends Exception(reason)

trait JsonSerializable {
  def toJson: Json
}

object JsonConversions {
  implicit def Any2Json(a: Any) = Json(a)

  implicit def Json2Boolean(j: Json) = j.asBoolean
  implicit def Json2Int(j: Json) = j.asInt
  implicit def Json2Long(j: Json) = j.asLong
  implicit def Json2Double(j: Json) = j.asDouble
  implicit def Json2BigDecimal(j: Json) = j.asBigDecimal
  implicit def Json2String(j: Json) = j.asString
  implicit def Json2Array(j: Json) = j.asArray
  implicit def Json2Object(j: Json) = j.asObject
}

object Json {
  type JArray = List[Json]
  type JObject = Map[String, Json]

  def build(any: Any): String = {
    def quote(s: String) = {
      "\"" + s.regexSub("""[\u0000-\u001f\u0080-\uffff/\"\\]""".r) { m =>
        m.matched.charAt(0) match {
          case '\r' => "\\r"
          case '\n' => "\\n"
          case '\t' => "\\t"
          case '"' => "\\\""
          case '\\' => "\\\\"
          case '/' => "\\/"
          case c => "\\u%04x" format c.asInstanceOf[Int]
        }
      } + "\""
    }
    def build0(a: Any): QuotedString = {
      val result = a match {
        case QuotedString(inner) => inner
        case null => "null"
        case v: Boolean => v.toString
        case v: Number => v.toString
        case list: Seq[_] => list.map(build0(_).inner).mkString("[", ",", "]")
        case map: Map[_, _] => (for ((key, value) <- map.iterator) yield { quote(key.toString) + ":" + build0(value).inner }).mkString("{", ",", "}")
        case json: Json => build0(json.any).toString
        case v => quote(v.toString)
      }
      QuotedString(result)
    }
    build0(any).toString
  }

  private def convert(any: Any): Json = any match {
    case m: java.util.Map[_, _] =>
      val map = new scala.collection.mutable.HashMap[String, Json]
      m.foreach { case (k, v) => map += ((k.toString, convert(v))) }
      Json(map)
    case l: java.util.List[_] =>
      val list = new scala.collection.mutable.ListBuffer[Any]
      l.foreach { e => list += convert(e) }
      Json(list.toList)
    case a => Json(a)
  }
  def parse(s: String): Json = convert((new ObjectMapper).readValue(s, classOf[Any]))
  def parse(reader: Reader): Json = convert((new ObjectMapper).readValue(reader, classOf[Any]))

  object Raw {
    def parseAny(s: String): Any = (new ObjectMapper).readValue(s, classOf[Any])
    def parseAny(reader: Reader): Any = (new ObjectMapper).readValue(reader, classOf[Any])

    type JArray = java.util.List[Any]
    type JObject = java.util.Map[String, Any]

    sealed case class Json(any: Any) {
      override def toString = any.toString
      def asNull = any.asInstanceOf[Null]
      def asBoolean = any.asInstanceOf[Boolean]
      def asInt = any.asInstanceOf[Int]
      def asLong = any.asInstanceOf[Long]
      def asDouble = any.asInstanceOf[Double]
      def asBigDecimal = any.asInstanceOf[BigDecimal]
      def asString = any.asInstanceOf[String]
      def asArray = any.asInstanceOf[Raw.JArray]
      def asObject = any.asInstanceOf[Raw.JObject]
    }

    implicit def Any2Json(a: Any) = Json(a)

    implicit def Any2Null(a: Any): Null = Json(a).asNull
    implicit def Any2Boolean(a: Any) = Json(a).asBoolean
    implicit def Any2Int(a: Any) = Json(a).asInt
    implicit def Any2Long(a: Any) = Json(a).asLong
    implicit def Any2Double(a: Any) = Json(a).asDouble
    implicit def Any2BigDecimal(a: Any) = Json(a).asBigDecimal
    implicit def Any2String(a: Any) = Json(a).asString
    implicit def Any2Array(a: Any) = Json(a).asArray
    implicit def Any2Object(a: Any) = Json(a).asObject
  }
}

