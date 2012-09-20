package com.ibm.haploid

package core

package util

package space

import org.junit.Assert.assertTrue
import org.junit.Test

@Test private class SpaceTest {

  def printIf(s: String) = if (false) println(s)

  @Test def testBasic = {
    val big = (1024 * 1024 * 1024L).kilobytes
    assertTrue(1024 * 1024 * 1024 * 1024L == big.inBytes)
    assertTrue(1024 * 1024 * 1024L == big.inKilobytes)
    assertTrue(1024 * 1024 == big.inMegabytes)
    assertTrue(1024 == big.inGigabytes)
    assertTrue(1 == big.inTerabytes)
    assertTrue((1 / 1024) == big.inPetabytes)
    assertTrue((1 / (1024 * 1024)) == big.inExabytes)
  }

}

