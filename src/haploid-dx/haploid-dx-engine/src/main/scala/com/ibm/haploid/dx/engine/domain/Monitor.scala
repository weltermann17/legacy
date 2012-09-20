package com.ibm.haploid

package dx

package engine

package domain

import javax.xml.bind.annotation.adapters._
import javax.xml.bind.annotation._

import collection.mutable.ListBuffer

import akka.actor.actorRef2Scala
import akka.actor.{ Actor, ActorRef }
import akka.dispatch.{ Await, Future, Promise }
import akka.pattern.ask

import event.{ Collect, Exists }
import binding.ElementsAdapter

/**
 *
 */
@XmlType(propOrder = Array("size", "elements"))
@XmlAccessorType(XmlAccessType.PROPERTY)
abstract class Monitor[A]

  extends DomainObject {

  @XmlAttribute(required = true) def getSize = getElements.length

  @XmlJavaTypeAdapter(classOf[ElementsAdapter[Monitor[A]]]) def getElements = elements

  def add(element: A) = elements = elements :+ element

  private[this] var elements: Seq[A] = Vector.empty

}

/**
 * Data of a Monitor.
 */
trait MonitorData

/**
 *
 */
abstract class Elements(

  val elements: Seq[ActorRef])

  extends MonitorData

/**
 *
 */
trait MonitorFSM[M]

  extends DomainObjectFSM[MonitorData] {

  whenUnhandled {
    case Event(collect @ Collect(collector), data) if data.isInstanceOf[Elements] ⇒
      collector ! data
      data.asInstanceOf[Elements].elements.foreach(_ ! collect)
      stay
  }

}

