package eu.man

package phevos

package dx

package util

import org.junit.Assert.assertTrue
import org.junit.Test
import com.ibm.haploid.core.config
import com.ibm.haploid.core.logger

import scala.xml._

@Test private class UtilTest {

  @Test def test1 = {
    println(dummy)
    assertTrue("application" == dummy)
  }

  @Test def testGetRecentFileAsString = {

    val client = new FTP
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

    val client = new FTP
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
