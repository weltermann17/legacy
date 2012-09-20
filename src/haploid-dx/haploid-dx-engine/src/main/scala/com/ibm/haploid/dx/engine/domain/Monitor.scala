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
  
  private[this] var _size: Option[Int] = None
  
  private[this] var elements: Seq[A] = Vector.empty
  
  @XmlAttribute(required = true) def getSize = this._size match {
    case Some(i) => i
    case None => getElements.length
  }

  @XmlJavaTypeAdapter(classOf[ElementsAdapter[Monitor[A]]]) def getElements = elements

  def add(element: A) = elements = elements :+ element
  
  def withSize(size: Int) = {
  	_size = Some(size)
  	this
  }

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
    case Event(collect @ Collect(collector, depth), data) if data.isInstanceOf[Elements] ⇒
      collector ! CollectResult(data, depth match {
        case Some(i) if i > 1 ⇒
          data.asInstanceOf[Elements].elements.size
        case None =>
          data.asInstanceOf[Elements].elements.size
        case _ ⇒
          0
      }, Some(data.asInstanceOf[Elements].elements.size))

      depth match {
        case Some(i) if i > 1 ⇒
          data.asInstanceOf[Elements].elements.foreach(_ ! Collect(collector, Some(i - 1)))
        case None =>
          data.asInstanceOf[Elements].elements.foreach(_ ! Collect(collector, None))
        case _ ⇒
      }

      stay
  }

}

