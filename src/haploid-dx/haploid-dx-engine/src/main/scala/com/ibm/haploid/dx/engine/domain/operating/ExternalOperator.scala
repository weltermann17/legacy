package com.ibm.haploid

package dx

package engine

package domain

package operating

import java.io.{ InputStream, ByteArrayOutputStream }
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{ CountDownLatch, TimeUnit }

import javax.xml.bind.annotation._

import scala.collection.JavaConversions.seqAsJavaList

import com.ibm.haploid.dx.engine.domain.binding.CDataAdapter

import core.operatingsystem
import core.concurrent.{ spawn, scheduleOnce }
import core.service.{ Success, Failure }
import core.util.io.Base64OutputStream
import core.util.time.timeMillis

import binding._

/**
 * Input
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "external-operation-detail")
abstract class ExternalOperationDetail(

  val scriptresource: String,

  val timeout: Long)

  extends OperationDetail("script") {

  def this() = this(null, -1L)

}

/**
 * Output
 */
@SerialVersionUID(1001L)
@XmlType(name = "script-operation-result-detail")
case class ExternalOperationResultDetail(

  private val success: Boolean,

  @xmlJavaTypeAdapter(classOf[CDataAdapter]) reason: String,

  @xmlJavaTypeAdapter(classOf[CDataAdapter]) console: String,

  @xmlJavaTypeAdapter(classOf[CDataAdapter]) logging: String,

  @xmlAttribute(required = true) durationinmilliseconds: Long)

  extends OperationResultDetail(success) {

  def this() = this(false, null, null, null, -1L)

}

/**
 *
 */
trait ExternalOperator

  extends OperatorBase with Local with FileHandler {

  protected[this] def doPreProcessing(input: ProcessResult) = {
    val details = operation.detail.asInstanceOf[ExternalOperationDetail]
    val path = Paths.get("scripts/" + operatingsystem + "/" + details.scriptresource + scriptextension)
    copyTextResourceToWorkingDirectory(path) match {
      case succ @ Success(fullpath) ⇒
        fullpath.toFile.setExecutable(true)
        commandline = operatingsystem match {
          case "unix" ⇒ fullpath.toString
          case "windows" ⇒ scriptbasecommandline + " \"" + fullpath + "\""
        }
        debug("commandline : " + commandline)
        timeout = details.timeout
        succ
      case fail @ Failure(_) ⇒ fail
    }
  }

  protected[this] def doProcessing(input: ProcessResult) = try {
    handleProcess({
      val (returncode, ms) = timeMillis(startProcess.waitFor)
      duration = ms
      debug("Process completed after : " + ms + " ms.")
      returncode
    })
  } catch {
    case e ⇒
      error("Unhandled exception : " + e)
      Failure(e)
  }

  protected[this] def doPostProcessing(input: ProcessResult) = input

  protected[this] def extractReasonFromConsoleOutput(returncode: Int, console: String): String = returncode match {
    case 0 ⇒ "External operation succeeded, returncode = 0"
    case e ⇒ "External operation failed, returncode = " + e
  }

  private[this] def startProcess = {
    val processbuilder = new ProcessBuilder(prepareCommands(commandline))
    debug(processbuilder.command.toString)
    processbuilder.directory(workingdirectory.toFile)
    processbuilder.redirectErrorStream(true)
    process.set(processbuilder.start)
    captureOutput(process.get.getInputStream)
    timeoutkiller
    process.get
  }

  private def prepareCommands(commands: String): List[String] = {
    debug(commands)
    commands.split(" ").toList
  }

  private def handleProcess(returncode: Int): ProcessResult = {
    timeoutkiller.cancel
    latch.await(1000, TimeUnit.MILLISECONDS)
    val console = processoutput.get match {
      case null ⇒ "Failed to capture console (broken pipe)."
      case output ⇒ new String(output.toByteArray, consolecharset)
    }
    val reason = extractReasonFromConsoleOutput(returncode, console)
    debug(reason)
    debug("Captured output starts on next line.\n" + console, true)
    returncode match {
      case 0 ⇒ Success(ExternalOperationResultDetail(true, null, console, currentLog, duration))
      case e ⇒ Failure(OperationFailed(ExternalOperationResultDetail(false, reason, console, currentLog, duration)))
    }
  }

  private[this] def captureOutput(in: InputStream) = {
    val bos = new ByteArrayOutputStream(buffersize)
    processoutput.set(bos)
    processpipe.set(in)
    spawn {
      var total = 0
      var b64 = new Base64OutputStream(processoutput.get)
      try {
        var exceeded = false
        val buffer = new Array[Byte](buffersize)
        var len = -1
        while (-1 < { len = processpipe.get.read(buffer); len }) {
          if (total + len < maxloggingperoperationsize) {
            b64.write(buffer, 0, len)
            total += len
          } else {
            exceeded = true
          }
        }
        warning("console truncated because it exceeded 'maxloggingperoperationsize' : " + (total + len))
      } catch {
        case e: java.io.IOException ⇒
        case e ⇒ warning("captureOutput failed : " + e)
      } finally {
        if (null != b64) b64.close
        debug("Capturing output completed : " + total + " bytes")
        latch.countDown
      }
    }
  }

  private[this] lazy val timeoutkiller = {
    debug("Scheduled timeout : " + timeout + " ms")
    scheduleOnce(timeout) {
      if (null != process.get) {
        debug("Timeout after " + timeout + " ms. Try to kill external process.")
        process.get.destroy
        process.set(null)
      } else {
        debug("Timeout after " + timeout + " ms. Process already killed.")
      }
    }
  }

  private[this] val processpipe = new AtomicReference[InputStream]

  private[this] val processoutput = new AtomicReference[ByteArrayOutputStream]

  private[this] val process = new AtomicReference[Process]

  private[this] val latch = new CountDownLatch(1)

  private[this] val buffersize = 2048

  private[this] var commandline: String = null

  private[this] var timeout: Long = -1L

  private[this] var duration: Long = -1L

}

