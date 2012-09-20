package com.ibm.haploid

package bootstrapper

import java.util.concurrent.atomic.AtomicReference
import java.io.{ InputStreamReader, OutputStreamWriter, PrintWriter, LineNumberReader }

import scala.collection.JavaConversions.propertiesAsScalaMap
import scala.collection.JavaConversions.seqAsJavaList

import core.concurrent._
import core.logger

import HasTerminationHook._

object BootStrapper extends App with HasTerminationHook {

  private[this] var process = new AtomicReference[Process]
  private[this] var startcount = 0

  addShutdownHook(() => killChild)

  def onTermination = {
    killChild
    0
  }

  private def killChild = {
    if (null != process.get) {
      val processwriter = new PrintWriter(new OutputStreamWriter(process.get.getOutputStream))
      processwriter.println(terminationtoken)
      processwriter.close
      process.set(null)
      System.out.println("Killed child process (" + mainclass + ").")
      System.out.flush
    }
  }

  private def continue(code: Int) = {
    startcount += 1
    exitcodetostoprestarting != code && (-1 < maximumrestarts && startcount < maximumrestarts)
  }

  private def pause = Thread.sleep(pausebeforerestart)

  private def unwanted: ((String, String)) => Boolean = {
    case (k, v) =>
      !k.startsWith("java") && !k.startsWith("sun") && !k.startsWith("os") && !k.startsWith("user") && !k.startsWith("file") && !k.startsWith("path") && !k.startsWith("line") && !k.startsWith("awt")
  }

  private def startProcess = {
    val classpath = management.ManagementFactory.getRuntimeMXBean.getClassPath
    val properties = System.getProperties.toList
      .filter(unwanted)
      .map { case (k, v) => ("-D" + k + "=" + v) }
    val commands = List(
      List("java"),
      properties,
      jvmoptions,
      List("-server"),
      List("-cp"),
      List(classpath),
      List(mainclass)).flatten
    val processbuilder = new ProcessBuilder(commands)
    processbuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
    processbuilder.redirectError(ProcessBuilder.Redirect.INHERIT)
    logger.debug(processbuilder.command.toString)
    process.set(processbuilder.start)
    process.get
  }

  try {
    if (restart) {
      while (continue(startProcess.waitFor)) pause
    } else {
      startProcess.waitFor
    }
    process.set(null)
  } catch {
    case e: InterruptedException => killChild
    case e => e.printStackTrace
  } finally {
    System.exit(0)
  }

}

