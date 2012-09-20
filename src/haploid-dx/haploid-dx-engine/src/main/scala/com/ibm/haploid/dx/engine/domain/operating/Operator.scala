package com.ibm.haploid

package dx

package engine

package domain

package operating

import java.io.{ StringWriter, PrintWriter, OutputStreamWriter, ByteArrayOutputStream }
import java.nio.file.Path

import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlElement }

import akka.actor.actorRef2Scala
import akka.event.{ Logging, LoggingAdapter }
import akka.pattern.ask

import core.inject.BindingModule
import core.service.{ Success, Failure, Result }
import core.util.io.Base64OutputStream

import domain.{ Idle, DomainObjectState, DomainObjectFSM, DomainObject }
import event.{ ReceiverEvent, OperationResult, OperationExecute, OperationCreate, Collect }
import binding._

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
@XmlType(propOrder = Array("name", "status", "successes", "failures", "operation"))
case class Operator(

  event: OperationCreate,

  @xmlAttribute(required = true) name: String,

  @xmlAttribute(required = true) status: String,

  @xmlAttribute(required = true) successes: Long,

  @xmlAttribute(required = true) failures: Long)

  extends DomainObject {

  private def this() = this(null, null, null, -1L, -1L)

  @XmlElement(nillable = true) val operation = if (null != event) event.id else null

}

/**
 *
 */
trait OperatorBase {

  val operation: OperationCreate

  val basedirectory: Path

  val log: LoggingAdapter

  val timeout: Long

  type ProcessResult = Result[Any]

  def apply = try {
    debug("Processing : " + operation)
    (doPreProcessing(Success(())) match {
      case succ @ Success(_) ⇒ doProcessing(succ) match {
        case succ @ Success(_) ⇒ doPostProcessing(succ)
        case fail @ Failure(_) ⇒ fail
      }
      case fail @ Failure(_) ⇒ fail
    }) match {
      case succ @ Success(_) ⇒ debug("Processing succeeded : " + operation); succ
      case fail @ Failure(_) ⇒ error("Processing failed : " + operation); fail
    }
  } catch {
    case e: OutOfMemoryError ⇒ core.terminateJvm(e, -1)
    case e ⇒
      error("Unhandled exception : " + e)
      Failure(e)
  }

  protected[this] def doPreProcessing(input: ProcessResult): ProcessResult

  protected[this] def doProcessing(input: ProcessResult): ProcessResult

  protected[this] def doPostProcessing(input: ProcessResult): ProcessResult

  protected[this] def debug(message: ⇒ String, skip: Boolean = false) = {
    if (!skip) write(message)
    log.debug(logprefix + message)
  }

  protected[this] def info(message: ⇒ String) = { write(message); log.info(logprefix + message) }

  protected[this] def warning(message: ⇒ String) = { write(message); log.warning(logprefix + message) }

  protected[this] def error(message: ⇒ String) = { write(message); log.error(logprefix + message) }

  protected[this] def currentLog = try {
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
    case e ⇒
      warning("logging failed : " + e)
      null
  }

  private[this] def write(message: String): Unit = {
    if (maxloggingperoperationsize < logbuffer.getBuffer.length + message.length) {
      logbuffer.getBuffer.setLength(0)
      debug("max-logging-per-operation-size exceeded (" + (logbuffer.getBuffer.length + message.length) + " bytes). Need to reset logging buffer.")
    }
    logcounter += 1
    logwriter.println(logprefix + message)
  }

  private[this] def logprefix = "[" + operation.id + "] [" + logcounter + "] "

  private[this] val logbuffer = new StringWriter(2048)

  private[this] val logwriter = new PrintWriter(logbuffer)

  private[this] var logcounter = 0

}

/**
 *
 */
class OperatorFSM(

  val name: String,

  val timeout: Long,

  val operatorclass: Class[_ <: OperatorBase])(

    implicit bindingmodule: BindingModule)

  extends DomainObjectFSM[OperatorData] {

  private[this] val log = Logging(context.system, this)

  startWith(Idle, OperatorIdle(0, 0))

  when(Idle) {

    case Event(OperationExecute(operation, basedirectory), OperatorIdle(successes, failures)) ⇒
      val operator = operatorclass.getConstructors()(0).newInstance(operation, basedirectory, log, timeout.asInstanceOf[AnyRef]).asInstanceOf[OperatorBase]
      operator.apply match {
        case succ @ Success(_) ⇒
          journal ? ReceiverEvent(sender, OperationResult(succ))
          stay using OperatorIdle(successes + 1, failures)
        case fail @ Failure(_) ⇒
          journal ? ReceiverEvent(sender, OperationResult(fail))
          stay using OperatorIdle(successes, failures + 1)
      }

  }

  whenUnhandled {

    case Event(collect @ Collect(collector), OperatorIdle(successes, failures)) ⇒
      collector ! Operator(null, name, stateName.toString.toLowerCase, successes, failures)
      stay

  }

  initialize

}

