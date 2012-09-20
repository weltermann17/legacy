package com.ibm.haploid

package core

package file

import org.junit.Assert.assertTrue
import org.junit.Test

import file._

@Test private class FileTest {

  @Test def testTemp = {
    val d = temporaryDirectory
    val t = temporaryFileInDirectory(d)
    val t2 = temporaryFile
  }

}

