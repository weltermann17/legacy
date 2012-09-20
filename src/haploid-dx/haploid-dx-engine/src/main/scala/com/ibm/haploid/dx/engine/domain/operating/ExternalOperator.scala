package com.ibm.haploid

package dx

package engine

package domain

package operating

import collection.JavaConversions._

import java.io.{ InputStream, FileReader, ByteArrayOutputStream, StringReader, BufferedReader, PrintWriter, OutputStreamWriter }
import java.nio.file.{ Path, Paths, Files }
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{ CountDownLatch, TimeUnit }

import javax.xml.bind.annotation._
import javax.xml.bind.annotation.adapters._

import scala.collection.JavaConversions.seqAsJavaList

import com.ibm.haploid.dx.engine.domain.binding.CDataAdapter

import core.operatingsystem
import core.file.{ temporaryDirectory, deleteDirectory }
import core.concurrent.{ spawn, scheduleOnce }
import core.service.{ Success, Failure, Result }
import core.util.io.Base64OutputStream
import core.util.time.timeMillis
import core.util.text.{ stackTraceToBase64, stackTraceToString }
import core.util.process.killProcessWithChildren

import binding._

/**
 * Input
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "external-operation-detail")
abstract class ExternalOperationDetail(

  operatorname: String,

  val script: String,

  val modulname: String,

  val resources: Seq[String],

  val environmentvariables: Map[String, String],

  val logfile: String,

  val timeout: Long)

  extends OperationDetail(operatorname) {

  private def this() = this(null, null, null, null, null, null, -1)

  @XmlAttribute(required = true) def getScript = script

  @XmlJavaTypeAdapter(classOf[StringListAdapter]) def getResources = resources

  @XmlJavaTypeAdapter(classOf[PropertiesAdapter]) def getEnvironmentvariables = environmentvariables

  @XmlAttribute(required = true) def getTimeoutinmilliseconds = timeout

}

/**
 *
 */
trait ExternalOperator

  extends OperatorBase

  with Local

  with FileHandler {

  override protected[this] def internalPreProcessing: Result[Any] = {
    resetDirectories
    val details = operation.detail.asInstanceOf[ExternalOperationDetail]
    otherresources = details.resources.map(Paths.get(_))
    copyBinaryResourcesToWorkingDirectory(otherresources: _*) match {
      case succ @ Success(fullpath) ⇒
        val script = Paths.get(details.script + (if (details.script.contains(".")) "" else scriptextension))
        (if (script.endsWith("CATScript"))
          copyTextResourceToWorkingDirectory(script)
        else
          copyBinaryResourceToWorkingDirectory(script)) match {
          case succ @ Success(fullpath) ⇒
            fullpath.toFile.setExecutable(true)
            commandline = operatingsystem match {
              case "unix" ⇒ fullpath.toAbsolutePath.toString
              case "windows" ⇒ scriptbasecommandline + " \"" + fullpath.toAbsolutePath + "\""
            }
            debug("commandline : " + commandline)
            tempdirectory = temporaryDirectory.toPath
            val tempdir = tempdirectory.toAbsolutePath.toString
            val workdir = workingdirectory.toAbsolutePath.toString
            environmentvariables = details.environmentvariables
            if (null == environmentvariables) environmentvariables = Map.empty
            environmentvariables = environmentvariables ++ Map(
              "JAVA_TOOL_OPTIONS" -> "-XX:-UsePerfData",
              "catia_xco_appli" -> tempdirectory.resolve(".catia.appli").toAbsolutePath.toString,
              "catia_xc0_appli" -> tempdirectory.resolve(".catia.appli").toAbsolutePath.toString,
              "USERNAME" -> (math.abs(scala.util.Random.nextInt) % 100000).toString,
              "USER_HOME" -> tempdir,
              "USERPROFILE" -> tempdir,
              "ALLUSERSPROFILE" -> tempdir,
              "APPDATA" -> tempdir,
              "LOCALAPPDATA" -> tempdir,
              "CATTraDecDIR" -> workdir,
              "CATTraceSDir" -> workdir,
              "CATUserSettingPath" -> workdir,
              "TEMP" -> tempdir,
              "TMP" -> tempdir,
              "CATTemp" -> tempdir)
            debug("environmentvariables : " + environmentvariables)
            logfile = if (null == details.logfile) null else workingdirectory.resolve(details.logfile)
            debug("logfile : " + logfile)
            Success(())
        }
    }
  }

  override protected[this] def internalProcessing: Result[Any] = {
    Thread.sleep((math.abs(scala.util.Random.nextInt) % 15) * 1000)
    handleProcess(processReturnCode(startProcess.waitFor))
  }

  override protected[this] def internalPostProcessing: Result[Any] = {
    otherresources.foreach { resource ⇒ try { Files.delete(workingdirectory.resolve(resource)) } catch { case _ ⇒ } }
    if (null != tempdirectory) deleteDirectory(tempdirectory.toFile)
    Success(0)
  }

  override protected[this] lazy val getReason = reason

  override protected[this] lazy val getConsole = {
    if (!processlatch.await(5000, TimeUnit.MILLISECONDS)) processlatch.countDown
    processoutput.get match {
      case null ⇒ "Failed to capture console (broken pipe)."
      case output ⇒ new String(output.toByteArray, consolecharset)
    }
  }

  override protected[this] lazy val getLogfile = {
    logfilelatchwrite.countDown
    if (!logfilelatchread.await(5000, TimeUnit.MILLISECONDS)) logfilelatchread.countDown
    logfileoutput.get match {
      case null ⇒ "Failed to capture logfile (broken pipe)."
      case output ⇒ new String(output.toByteArray, consolecharset)
    }
  }

  protected[this] def processReturnCode(returncode: Int): Int = returncode

  protected[this] def extractReasonFromConsoleOutput(returncode: Int, console: String) = returncode match {
    case 0 ⇒ "External operation succeeded, returncode = 0"
    case c ⇒ "External operation failed, returncode = " + c
  }

  protected[this] def addEnvironmentVariable(name: String, value: String) = {
    environmentvariables = environmentvariables ++ Map(name -> value)
  }

  private[this] def startProcess = {
    val processbuilder = new ProcessBuilder(prepareCommands(commandline))
    processbuilder.directory(workingdirectory.toFile)
    processbuilder.environment.putAll(environmentvariables)
    processbuilder.redirectErrorStream(true)
    process.set(processbuilder.start)
    captureConsole(process.get.getInputStream)
    captureLogfile
    timeoutkiller
    process.get
  }

  private def prepareCommands(commands: String): List[String] = {
    debug(commands)
    commands.split(" ").toList
  }

  private def handleProcess(returncode: Int): Result[Any] = {
    timeoutkiller.cancel
    reason = extractReasonFromConsoleOutput(returncode, getConsole)
    returncode match {
      case 0 ⇒ Success(0)
      case c ⇒ Failure(new Exception(getClass.getName + " failed with returncode = " + c))
    }
  }

  private[this] def captureConsole(input: InputStream) = {
    processoutput.set(new ByteArrayOutputStream(buffersize))
    spawn {
      var total = 0
      var b64 = if (encodeoutputwithbase64) new Base64OutputStream(processoutput.get) else processoutput.get
      try {
        var exceeded = false
        val buffer = new Array[Byte](buffersize)
        var len = -1
        while (-1 < { len = input.read(buffer); len }) {
          if (total + len < maxloggingperoperationsize && !exceeded) {
            b64.write(buffer, 0, len)
            total += len
          } else {
            exceeded = true
          }
        }
        if (exceeded) warning("console truncated because it exceeded 'maxloggingperoperationsize' : " + (total + len))
      } catch {
        case e: java.io.IOException ⇒
        case e: Throwable ⇒ warning("capture console failed : " + e)
      } finally {
        if (null != b64) b64.close
        debug("Capturing console completed : " + total + " bytes")
        processlatch.countDown
      }
    }
  }

  private[this] def captureLogfile = {
    logfileoutput.set(new ByteArrayOutputStream(buffersize))
    spawn {
      var total = 0
      val b64 = if (encodeoutputwithbase64) new Base64OutputStream(logfileoutput.get) else logfileoutput.get
      var out = new PrintWriter(new OutputStreamWriter(b64, consolecharset))
      var in: BufferedReader = null
      var fin: FileReader = null
      try {
        if (null == logfile) {
          in = new BufferedReader(new StringReader(""))
        } else {
          while (null == in && 0 < logfilelatchwrite.getCount) {
            Thread.sleep(2000)
            try { fin = new FileReader(logfile.toFile); in = new BufferedReader(fin) } catch { case _ ⇒ }
          }
          debug("Capturing logfile started : " + logfile)
          Thread.sleep(2000)
        }
        var exceeded = false
        val buffer = new Array[Byte](buffersize)
        var len = -1
        while (0 < logfilelatchwrite.getCount) {
          in.readLine match {
            case null ⇒ Thread.sleep(2000)
            case line ⇒
              len = line.length
              if (total + len < maxloggingperoperationsize && !exceeded) {
                out.println(line)
                total += len
              } else {
                exceeded = true
              }
          }
        }
        if (exceeded) warning("logfile truncated because it exceeded 'maxloggingperoperationsize' : " + (total + len))
      } catch {
        case e: java.io.IOException ⇒
        case e: Throwable ⇒ debug(if (encodeoutputwithbase64) stackTraceToBase64(e) else stackTraceToString(e)); warning("capture logfile failed : " + e)
      } finally {
        if (null != fin) fin.close
        if (null != in) in.close
        if (null != out) out.close
        if (null != logfileoutput.get) logfileoutput.get.close
        debug("Capturing logfile completed : " + total + " bytes")
        logfilelatchread.countDown
      }
    }
  }

  private[this] lazy val timeoutkiller = {
    debug("Scheduled timeout : " + timeout + " ms")
    scheduleOnce(timeout) {
      if (null != process.get) {
        debug("Timeout after " + timeout + " ms. Try to kill external process.")
        killProcessWithChildren(process.get)
        process.set(null)
      } else {
        debug("Timeout after " + timeout + " ms. Process already killed.")
      }
    }
  }

  private[this] val processoutput = new AtomicReference[ByteArrayOutputStream]

  private[this] val process = new AtomicReference[Process]

  private[this] val processlatch = new CountDownLatch(1)

  private[this] val logfileoutput = new AtomicReference[ByteArrayOutputStream]

  private[this] val logfilelatchread = new CountDownLatch(1)

  private[this] val logfilelatchwrite = new CountDownLatch(1)

  private[this] val buffersize = 512

  protected[this] var commandline: String = null

  protected[this] var reason: String = null

  private[this] var logfile: Path = null

  private[this] var environmentvariables: Map[String, String] = Map.empty

  private[this] var otherresources: Seq[Path] = Seq.empty

  protected[this] var tempdirectory: Path = null

}

