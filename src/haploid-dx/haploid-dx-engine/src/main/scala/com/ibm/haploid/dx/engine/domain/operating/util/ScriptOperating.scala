package com.ibm.haploid

package dx

package engine

package domain

package operating

package util

import java.nio.file.Path

import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlAccessorType }

import akka.event.LoggingAdapter

import core.service._

import domain.operating.{ ExternalOperator, ExternalOperationDetail }
import event.OperationCreate

/**
 *
 */
@XmlType(name = "script-operation-detail")
case class ScriptOperationDetail(

  private val scr: String,

  private val modname: String,

  private val rsc: Seq[String],

  private val env: Map[String, String],

  private val lfile: String,

  private val tmout: Long)

  extends ExternalOperationDetail("script", scr, modname, rsc, env, lfile, tmout) {

  private def this() = this(null, null, null, null, null, -1L)

}

/**
 *
 */
class ScriptOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends ExternalOperator {

  type PreProcessingInput = Unit

  type ProcessingInput = Unit

  type PostProcessingInput = Unit

  type PostProcessingOutput = Unit

  protected[this] def doPreProcessing(input: Unit) = Success(input)

  protected[this] def doProcessing(input: Unit) = Success(input)

  protected[this] def doPostProcessing(input: Unit) = Success(input)

}

