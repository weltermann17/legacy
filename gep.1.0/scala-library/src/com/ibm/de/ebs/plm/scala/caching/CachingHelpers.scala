package com.ibm.de.ebs.plm.scala.caching

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter

import com.ibm.de.ebs.plm.scala.json.Json

object CachingHelpers {
  def write[K, V](cache: Cache[K, V], filepath: String)(implicit p: K => Json.JObject, q: V => Json.JObject) = {
    val writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath), "UTF-8")))
    try {
      cache.contents.foreach { k =>
        cache.get(k) match {
          case Some(v) =>
            val entry = Json.build(List(p(k), q(v)))
            writer.println(entry)
          case None =>
        }
      }
    } finally {
      writer.close
    }
  }
  def prefill[K, V](cache: Cache[K, V], filepath: String)(implicit p: Json.JObject => K, q: Json.JObject => V) = {
    val reader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "UTF-8"))
    try {
      var eof = false
      while (!eof) {
        val line = reader.readLine()
        eof = null == line
        if (!eof) {
          val entry = Json.parse(line).asArray
          cache.add(p(entry(0).asObject), q(entry(1).asObject))
        }
      }
    } finally {
      reader.close
    }
  }
}
