package com.ibm.haploid

package core

import org.junit.Assert.assertTrue
import org.junit.Test

@Test private class CoreTest {

  @Test def testCore = {
    val conf = config.toString
    assertTrue(0 < conf.length)
  }

}

