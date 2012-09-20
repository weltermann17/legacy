package com.ibm.haploid

package bootstrapper

import org.junit.Assert.assertTrue
import org.junit.Test

import com.typesafe.config.{ ConfigFactory, ConfigParseOptions, ConfigResolveOptions }

@Test private class BootstrapperTest {

  @Test def testMain = {
    // Main.main(null)
  }

}

object TestMain extends App {
  println("This is in TestMain.")
  System.exit(0) // not necessary unless -1001 is specified
}

