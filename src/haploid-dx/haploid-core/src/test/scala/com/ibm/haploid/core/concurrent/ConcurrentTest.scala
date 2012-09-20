package com.ibm.haploid

package core

package concurrent

import org.junit.Assert.assertTrue
import org.junit.Test

import akka.util.duration._
import akka.actor._
import com.typesafe.config.ConfigFactory

@Test private class ConcurrentTest {

  val print = true

  @Test def testInit = {
    schedule(0, 20) { assertTrue(1 == 1) }
    Thread.sleep(100)
  }

}
