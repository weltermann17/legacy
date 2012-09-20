package com.ibm.haploid

package core

package util

package json

import org.junit.Assert.assertTrue
import org.junit.Test

import io._
import time._
import Json._

@Test private class JsonTest {

  def printIf(s: String) = if (false) println(s)

  @Test def testJsonSimple = {
    val a = List(1, 2, 3)
    val b = parse[List[Int]]("[1, 2, 3]")
    assertTrue(a == b)
    assertTrue(generate(b) == "[1,2,3]")
  }

  @Test def testJsonHugeWithJerkson = {
    val ms = timeMillis {
      val i = getClass.getResourceAsStream("/huge.txt")
      val a = parse[Map[String, Any]](i)      
    }
    assertTrue(3000 > ms._2)
  }

  /**
   * the buildin test takes > 50sec on my pc, jerkson takes ~ 0.6 sec for the same input
   */  
  @Test def testJsonHugeWithBuiltIn = {
//    printNanos {
//      val i = getClass.getResourceAsStream("/huge.txt")
//      val s = new String(copyFully(i).toByteArray, "UTF-8")
//      println(s.length)
//      val a = scala.util.parsing.json.JSON.parseRaw(s)
//      println(a.size)
//    }
  }

}

