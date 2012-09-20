package com.ibm.haploid

package dx

package engine

package domain

package operating

package util

import java.nio.file.Path

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import javax.xml.bind.annotation.{ XmlRootElement, XmlElement, XmlAttribute, XmlType }

import akka.actor.Props
import akka.dispatch.{ Await, Future }
import akka.pattern.ask
import akka.event._

import core.service._
import core.inject.BindingModule
import journal.JournalEntry
import domain.binding._
import event._

/**
 *
 */
@XmlType(name = "move-operation-detail")
case class MoveOperationDetail(

  @xmlAttribute(required = true) from: String,

  @xmlAttribute(required = true) to: String)

  extends OperationDetail("move") {

  private def this() = this(null, null)

}

/**
 *
 */
class MoveOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase {

  type PreProcessingInput = Unit

  type ProcessingInput = Unit

  type PostProcessingInput = Unit

  type PostProcessingOutput = Unit

  protected[this] def doPreProcessing(input: Unit) = Success(input)

  protected[this] def doProcessing(input: Unit) = Success(input)

  protected[this] def doPostProcessing(input: Unit) = Success(input)

}

