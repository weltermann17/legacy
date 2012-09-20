package com.ibm.haploid.application

import com.ibm.haploid.io._
import com.ibm.haploid.io.NullOutputStream

object Main extends App {
  println(getClass.getName + " : Hello world.")
  val out = NullOutputStream
  val a = new Array[Byte](10000)
  val in = new java.io.ByteArrayInputStream(a)
  val o = copyFully(in)
  println("o size = " + o.size)
  val m = Map("bla" -> "foe", "ggg" -> 4711)
  println(m)
  println(getConfiguration)
}

