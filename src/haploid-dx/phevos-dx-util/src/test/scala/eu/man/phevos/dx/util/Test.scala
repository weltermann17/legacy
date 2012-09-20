package eu.man

package phevos

package dx

package util

import scala.xml.PrettyPrinter

import org.junit.Assert.assertTrue
import org.junit.Test

import eu.man.phevos.dx.util.interfaces.MTBPartIndexOrdering

@Test private class UtilTest {

  @Test def testOrdering = {

    println(MTBPartIndexOrdering("_A_").compare("_A_"))
    assertTrue(MTBPartIndexOrdering("_A_") == "_A_")

  }

  @Test def testGetRecentFileAsString = {

    val client = new FtpConnector
    if (client.isConnected) {
      val filename = client.getRecentFile
      println("recent file name : " + filename)

      val output = client.getFileAsString(filename)
      println("content of file)")
      println(output)

      assertTrue(output.length > 0)
    } else {
      assertTrue(true)
    }

  }

  @Test def testGetRecentFileAsXML = {

    val client = new FtpConnector
    if (client.isConnected) {
      val filename = client.getRecentFile
      println("recent file name : " + filename)

      val output = client.getFileAsXML(filename)
      println("content of file)")
      println(new PrettyPrinter(20, 2).format(output))

      assertTrue(output.length > 0)
    } else {
      assertTrue(true)
    }

  }
}
