package com.ibm.haploid

package core

package util

package raw

import org.junit.Assert.assertTrue
import org.junit.Test

@Test private class RawTest {

  def printIf(s: String) = if (false) println(s)

  @Test def testBasic = {
    assertTrue(java.util.Arrays.equals(Array[Byte](-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1), Raw("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF").asArray))
    assertTrue("12341234123412341234123412341234" == Raw("12341234123412341234123412341234").toString)
    assertTrue((NullRaw8 + NullRaw8) == NullRaw16)
    assertTrue("00000000000000000000000000000000" == NullRaw16.toString)
    assertTrue(java.util.Arrays.equals(Array[Byte](0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), NullRaw.asArray))
  }

}

