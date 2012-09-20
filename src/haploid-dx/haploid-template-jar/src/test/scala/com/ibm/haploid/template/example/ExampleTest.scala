package com.ibm.haploid

package template

package example

import org.junit.Assert.assertTrue
import org.junit.Test

@Test private class ExampleTest {

  @Test def test1 = {
    val example = new Example
    assertTrue("You called Example.callMe. Thank you." == example.callMe)
  }

}

