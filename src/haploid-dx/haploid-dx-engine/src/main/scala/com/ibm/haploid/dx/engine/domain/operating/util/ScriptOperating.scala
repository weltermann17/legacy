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
@XmlType(name = "script-operation-detail")
case class ScriptOperationDetail(

  @xmlAttribute(required = true) script: String,

  @xmlAttribute(required = true) timeoutinmilliseconds: Long)

  extends ExternalOperationDetail(script, timeoutinmilliseconds) {

  private def this() = this(null, -1L)

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

}

