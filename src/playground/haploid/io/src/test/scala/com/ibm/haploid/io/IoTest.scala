package com.ibm.haploid.io

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter

import org.junit.Assert.assertTrue
import org.junit.Test

@Test
private class IoTest {

  @Test def testNullStream = {
    val nullstream = NullOutputStream
    (1 to 100).foreach(nullstream.write)
    assertTrue("" == nullstream.toString)
  }

  @Test
  def testBinaryFormat = {
    val array = java.nio.ByteBuffer.allocateDirect(10000)
    (1 to 10).foreach(_ => {
      val out = new BinaryFormatByteBuffer(array, 0, 10000)
      val s = "This is a test string."
      val longs = """
abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789
abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789
abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789
abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789
abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789
abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789
abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789
abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789
abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789
abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789
"""
      val map = Map("a" -> "value1", "b" -> 42, "c" -> 3.1415, "d" -> Map("aa" -> "value2"), "e" -> List(1, "s", false, 1000000000000000L), "f" -> Some(Some(3.toByte)))
      val list = List(1, "s", true, 3.1415, Map("a" -> "string"))
      out.writeBoolean(true)
      out.writeBoolean(false)
      out.writeByte(0)
      out.writeByte(255.toByte)
      out.writeChar('@')
      out.writeShort(32755)
      out.writeInt(42)
      out.writeInt(100000000)
      out.writeLong(10000000000000L)
      out.writeDouble(3.141592)
      out.writeShortString(s)
      out.writeString(s)
      out.writeString(longs)
      out.writeMap(map)
      out.writeList(list)
      out.writeOption(None)
      out.writeOption(Some("Hello"))

      val in = new BinaryFormatByteBuffer(array, 0, 10000)
      assertTrue(true == in.readBoolean)
      assertTrue(false == in.readBoolean)
      assertTrue(0 == in.readByte)
      assertTrue(255.toByte == in.readByte)
      assertTrue('@' == in.readChar)
      assertTrue(32755 == in.readShort)
      assertTrue(42 == in.readInt)
      assertTrue(100000000 == in.readInt)
      assertTrue(10000000000000L == in.readLong)
      assertTrue(3.141592 == in.readDouble)
      assertTrue(s == in.readShortString)
      assertTrue(s == in.readString)
      assertTrue(longs == in.readString)
      assertTrue(map == in.readMap)
      assertTrue(list == in.readList)
      assertTrue(None == in.readOption)
      assertTrue(Some("Hello") == in.readOption)
    })
  }
  
  @Test def testResources = {
    val r = this.getClass.getResource("/com/ibm/haploid/io/reference.conf")
    println("resource " + r)
    println("getConfiugration"  + getConfiguration)
    ()
  }

}