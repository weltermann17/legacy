package com.ibm.haploid

package dx

package engine

package domain

package operating

import java.io.{ StringWriter, PrintWriter, OutputStreamWriter, ByteArrayOutputStream }
import java.nio.file.Path

import javax.xml.bind.annotation.{ XmlType, XmlRootElement }

import scala.annotation.tailrec

import akka.actor.{ actorRef2Scala, FSM, ActorPath }
import akka.event.{ Logging, LoggingAdapter }

import com.ibm.haploid.core.service.{ ServiceException, Result }
import com.ibm.haploid.dx.engine.domain.DomainObjectFSM
import com.ibm.haploid.dx.engine.event.OperationCreate

import core.inject.BindingModule
import core.service.{ Success, Failure }
import core.util.io.Base64OutputStream
import core.util.text.{ stackTraceToString, stackTraceToBase64 }
import core.util.time.timeMillis

import domain.binding._
import domain.{ Idle, DomainObjectState, DomainObject }
import event.{ ReceiverEvent, OperationResult, OperationExecute }

/**
 * States of an Operator.
 */
sealed trait OperatorState extends DomainObjectState

/**
 * Data of an Operator.
 */
sealed trait OperatorData

case class OperatorIdle(successes: Long, failures: Long) extends OperatorData

/**
 *
 */
@XmlRootElement(name = "operator")
@XmlType(propOrder = Array("name", "successes", "failures"))
case class Operator(

  @xmlAttribute(required = true) name: String,

  @xmlAttribute(required = true) successes: Long,

  @xmlAttribute(required = true) failures: Long)

  extends DomainObject {

  private def this() = this(null, -1L, -1L)

}

case class OperatorUpdate(name: String, succeeded: Boolean)

/**
 *
 */
trait OperatorBase {

  val operation: OperationCreate

  val basedirectory: Path

  val log: LoggingAdapter

  val timeout: Long

  type ProcessResult = Result[Any]

  def apply(input: PreProcessingInput, retries: Int): OperationResultDetail = try {

    apply$(input, retries)

  } catch {
    case e: OutOfMemoryError ⇒ core.terminateJvm(e, -1)
    case e: Throwable ⇒
      error("Unhandled exception : " + e)
      val stacktrace = if (encodeoutputwithbase64) stackTraceToBase64(e) else stackTraceToString(e)
      OperationResultDetail(Failure(e), getReason, getConsole, getLogfile, getInternalLogging, stacktrace, -1L)
  }

  def apply$(input: PreProcessingInput, retries: Int): OperationResultDetail = {
    debug("Starting operation : " + operation)
    timeMillis(
      internalPreProcessing match {
        case Success(_) ⇒ doPreProcessing(input) match {
          case Success(output) ⇒ internalProcessing match {
            case Success(_) ⇒ doProcessing(output) match {
              case Success(output) ⇒ internalPostProcessing match {
                case Success(_) ⇒ doPostProcessing(output)
                case fail @ Failure(_) ⇒ fail
              }
              case fail @ Failure(_) ⇒ fail
            }
            case fail @ Failure(_) ⇒ fail
          }
          case fail @ Failure(_) ⇒ fail
        }
        case fail @ Failure(_) ⇒ fail
      }) match {
        case (succ @ Success(result), durationinmilliseconds) ⇒
          debug("Operation completed successfully after " + durationinmilliseconds + " ms : " + operation + " result : " + result)
          if (null != getConsole) debug("Captured console output starts on next line.\n" + getConsole, true)
          if (null != getLogfile) debug("Captured logfile starts on next line.\n" + getLogfile, true)
          OperationResultDetail(succ, getReason, getConsole, getLogfile, getInternalLogging, null, durationinmilliseconds)
        case (fail @ Failure(throwable), durationinmilliseconds) ⇒
          val stacktrace = if (encodeoutputwithbase64) stackTraceToBase64(throwable) else stackTraceToString(throwable)
          error("Operation failed after " + durationinmilliseconds + " ms : " + operation + " reason : " + throwable)
          if (null != getReason) debug("Reason starts on next line.\n" + getReason)
          if (null != stacktrace) debug("Stack trace starts on next line.\n" + stacktrace)
          if (null != getConsole) debug("Captured console output starts on next line.\n" + getConsole, true)
          if (null != getLogfile) debug("Captured logfile starts on next line.\n" + getLogfile, true)
          OperationResultDetail(fail, getReason, getConsole, getLogfile, getInternalLogging, stacktrace, durationinmilliseconds)
      }

  }

  type PreProcessingInput

  type ProcessingInput

  type PostProcessingInput

  type PostProcessingOutput

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] 

  protected[this] def doProcessing(input: ProcessingInput): Result[PostProcessingInput]

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput]

  protected[this] def internalPreProcessing: Result[Any] = Success(())

  protected[this] def internalProcessing: Result[Any] = Success(())

  protected[this] def internalPostProcessing: Result[Any] = Success(())

  protected[this] def getReason: String = null

  protected[this] def getConsole: String = null

  protected[this] def getLogfile: String = null

  protected[this] def getInternalLogging: String = try {
    if (0 < logbuffer.getBuffer.length) {
      val bos = new ByteArrayOutputStream(1024)
      val b64 = new Base64OutputStream(bos)
      val writer = new OutputStreamWriter(b64)
      writer.write(logbuffer.toString)
      writer.close
      new String(bos.toByteArray, "UTF-8")
    } else {
      null
    }
  } catch {
    case e: OutOfMemoryError ⇒ core.terminateJvm(e, -1)
    case e: Throwable ⇒
      warning("logging failed : " + e)
      null
  }

  protected[this] def debug(message: ⇒ String, skip: Boolean = false) = {
    if (!skip) write(message)
    log.debug(logprefix + message)
  }

  protected[this] def info(message: ⇒ String) = { write(message); log.info(logprefix + message) }

  protected[this] def warning(message: ⇒ String) = { write(message); log.warning(logprefix + message) }

  protected[this] def error(message: ⇒ String) = { write(message); log.error(logprefix + message) }

  private[this] def write(message: String): Unit = {
    if (maxloggingperoperationsize < logbuffer.getBuffer.length + message.length) {
      logbuffer.getBuffer.setLength(0)
      debug("max-logging-per-operation-size exceeded (" + (logbuffer.getBuffer.length + message.length) + " bytes). Need to reset logging buffer.")
    }
    logcounter += 1
    logwriter.println(logprefix + message)
  }

  private[this] def logprefix = "[" + operation.id + "] [" + logcounter + "] "

  private[this] val logbuffer = new StringWriter(512)

  private[this] val logwriter = new PrintWriter(logbuffer)

  private[this] var logcounter = 0

}

/**
 *
 */
class OperatorFSM(

  val name: String,

  val timeout: Long,

  val repeats: Int,

  val repeatTimeout: Long,

  val operatorclass: Class[_ <: OperatorBase])(

    implicit bindingmodule: BindingModule)

  extends DomainObjectFSM[OperatorData] {

  private[this] lazy val operatorMonitor = context.system.actorFor(ActorPath.fromString("akka://default/user/engine/operators/"))

  override val log = core.newLogger(operatorclass.getName) 
    
  startWith(Idle, OperatorIdle(0, 0))

  private[this] def updateMonitor(succeeded: Boolean) =
    operatorMonitor ! OperatorUpdate(name, succeeded)

  when(Idle) {

    case Event(OperationExecute(operation, basedirectory, input), OperatorIdle(successes, failures)) ⇒
    
      @tailrec
      def apply$(retries: Int): FSM.State[DomainObjectState, OperatorData] = {
      
    	log.debug("Starting operation on operator : " + operatorclass.getName)
      
    	if (repeats > retries) log.info("Need to retry operation (" + ((repeats - retries) + 1) + "/" + repeats + ") : " + operation)

        val operator = operatorclass
          .getConstructors()(0)
          .newInstance(operation, basedirectory.toAbsolutePath, log, timeout.asInstanceOf[AnyRef])
          .asInstanceOf[OperatorBase]

        operator.apply(input.asInstanceOf[operator.PreProcessingInput], retries) match {
          case succ @ OperationResultDetail(Success(_), _, _, _, _, _, _) ⇒
            journal ! ReceiverEvent(sender, OperationResult(succ))
            updateMonitor(true)
            stay using OperatorIdle(successes + 1, failures)
          case fail @ OperationResultDetail(Failure(e), _, _, _, _, _, _) if (!e.isInstanceOf[ServiceException] && (0 < retries)) ⇒
            log.warning("Execution failed for a technical reason. Retry in " + repeatTimeout + " ms (" + ((repeats - retries) + 1) + "/" + repeats + "). Reason: " + e.getMessage)
            val variation = (math.abs(scala.util.Random.nextInt) % 15) * 1000
            Thread.sleep(repeatTimeout + variation)
            apply$(retries - 1)
          case fail @ OperationResultDetail(Failure(_), _, _, _, _, _, _) ⇒
            journal ! ReceiverEvent(sender, OperationResult(fail))
            updateMonitor(false)
            stay using OperatorIdle(successes, failures + 1)
        }

      }

      apply$(repeats)

  }

  initialize

}

