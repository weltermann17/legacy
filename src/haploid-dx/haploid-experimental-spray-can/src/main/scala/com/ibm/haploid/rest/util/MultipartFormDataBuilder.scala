package com.ibm.haploid.rest.util
import cc.spray.http.HttpContent
import scala.util.Random
import cc.spray.http.ContentType
import cc.spray.http.HttpCharsets._
import cc.spray.http.MediaTypes._
import java.io.InputStream
import java.io.File
import java.io.FileInputStream

class MultipartFormDataBuilder {

  val boundary = "VFC-Plauen-" + Random.nextInt()
  val charset = `UTF-8`
  var fields: List[(Map[String, String], Array[Byte])] = List()

  def append(map: Map[String, String], content: Array[Byte]): MultipartFormDataBuilder = {
    fields = fields ++ List((map, content))
    this
  }

  def append(fieldName: String, value: String): MultipartFormDataBuilder = {
    val map = Map("Content-Disposition" -> new String("form-data; name=\"" + fieldName + "\""))
    append(map, value.getBytes(charset.value))
  }

  def append(fieldName: String, contentType: String, fromFile: String): MultipartFormDataBuilder = {
    val file = new File(fromFile)
    append(fieldName, file.getName(), contentType, new FileInputStream(file))
  }

  def append(fieldName: String, fileName: String, contentType: String, stream: InputStream): MultipartFormDataBuilder = {
    val map = Map(
      "Content-Disposition" -> new String("form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\""),
      "Content-Type" -> contentType)

    val bytes = Stream.continually(stream.read).takeWhile(-1 <).map(_.toByte).toArray
    append(map, bytes)
  }

  def get: HttpContent = {
    var body: Array[Byte] = Array()

    fields.foreach { field ⇒
      val header = {
        var out = "--" + boundary + "\n"
        field._1.foreach(entry ⇒ out += entry._1 + ": " + entry._2 + "\n")
        out
      }

      body = body ++ header.concat("\n").getBytes(charset.value) ++ field._2 ++ "\n".getBytes(charset.value)
    }

    body = body ++ "--".concat(boundary).concat("--").getBytes(charset.value)

    HttpContent(ContentType(new `multipart/form-data`(Some(boundary)), charset), body)
  }

}

object MultipartFormDataBuilder {

  def apply: MultipartFormDataBuilder = new MultipartFormDataBuilder

  def apply(map: Map[String, String], content: Array[Byte]): MultipartFormDataBuilder = {
    val b = apply
    b.append(map, content)
    b
  }

  def apply(fieldName: String, value: String): MultipartFormDataBuilder = {
    val b = apply
    b.append(fieldName, value)
    b
  }

  def apply(fieldName: String, contentType: String, fromFile: String): MultipartFormDataBuilder = {
    val b = apply
    b.append(fieldName, contentType, fromFile)
    b
  }

  def apply(fieldName: String, fileName: String, contentType: String, stream: InputStream): MultipartFormDataBuilder = {
    val b = apply
    b.append(fieldName, fileName, contentType, stream)
    b
  }

}