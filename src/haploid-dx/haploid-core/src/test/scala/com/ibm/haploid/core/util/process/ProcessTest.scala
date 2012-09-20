package com.ibm.haploid

package core

package util

package process

import scala.collection.JavaConversions.seqAsJavaList

import org.junit.Assert.assertTrue
import org.junit.Test

@Test private class ProcessTest {

  def printIf(s: String) = if (true) println(s)

  @Test def testBasic = {

//    printIf("current process " + getCurrentProcessId)
//
//    val processbuilder = operatingsystem match {
//      case "windows" ⇒ new ProcessBuilder(List[String]("c:\\windows\\system32\\cmd.exe", "/c", "Notepad.exe"))
//      case "unix" ⇒ new ProcessBuilder(List[String]("ls", " -lR", " /tmp"))
//      case _ ⇒ new ProcessBuilder
//    }
//    val process = processbuilder.start
//    printIf("spawned process " + getProcessId(process))
//
//    Thread.sleep(10000)
//    killProcessWithChildren(process)
//
//    printIf("process returncode " + process.waitFor)

  }

}

