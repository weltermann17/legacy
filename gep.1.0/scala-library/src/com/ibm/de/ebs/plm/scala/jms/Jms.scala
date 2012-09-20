package com.ibm.de.ebs.plm.scala.jms

import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Logger

import com.ibm.de.ebs.plm.jms.implementation.reactor.QueueConnectionFactory
import com.ibm.de.ebs.plm.scala.concurrent.ops.OnlyOnce
import com.ibm.de.ebs.plm.scala.util.Timers.now
import com.ibm.de.ebs.plm.scala.util.Timers.time

import javax.jms.JMSException
import javax.jms.QueueConnection
import javax.jms.QueueSession
import javax.jms.TextMessage

trait JmsUsing {
  val jms: Jms
}

trait Jms {
  def createSession: QueueSession
  def request(queue: String, text: String, attributes: List[(String, String)] = Nil, timeout: Long = -1, initonly: Boolean = false): Option[String]
}

case class ReactorBasedJms(configuration: String, logger: Logger) extends Jms with OnlyOnce {

  def createSession = { connection.synchronized { connection.createQueueSession(true, 0) } }

  def request(queue: String, text: String, attributes: List[(String, String)], tmout: Long, initonly: Boolean = false): Option[String] = {
    val timeout = if (0 > tmout) defaulttimeout else tmout
    if (tryInit(timeout) && !initonly) {
      val session = createSession
      try {
        val requestor = session.asInstanceOf[com.ibm.de.ebs.plm.jms.implementation.reactor.QueueSession].createQueueRequestor(session.createQueue(queue))
        val in = session.createTextMessage(text)
        attributes.foreach { case (n, v) => in.setStringProperty(n, v) }
        val start = now
        val end = start + timeout
        var result: Option[String] = None
        while (result.isEmpty && now < end) {
          try {
            val duration = time(result = requestor.request(in, timeout.asInstanceOf[Int]).asInstanceOf[TextMessage] match { case null => None case m => m.getText match { case s: String => Some(s) case _ => None } })
            if (result.isEmpty) {
              logger.warning("Jms : result.isEmpty : " + queue)
              Thread.sleep(veryshortinterval)
            } else {
              if (duration > maxduration.get) maxduration.set(duration)
              logger.info("Jms : " + duration + "ms, max " + maxduration.get + "ms, " + queue)
            }
          } catch {
            case e:
              JMSException =>
              logger.severe("Jms : " + e.toString)
              Thread.sleep(shortinterval)
          }
        }
        result
      } catch {
        case e: JMSException => logger.severe(e.toString); None
        case _: ClassCastException => None
      } finally {
        session.close
      }
    } else {
      None
    }
  }

  private def tryInit(timeout: Long) = {
    var done = false
    try {
      val elapsed = now + timeout
      while (now < elapsed && !done) {
        try { done = init }
        catch { case _: JMSException => Thread.sleep(longinterval) }
      }
    }
    if (!done) {
      logger.severe("ReactorBasedJms configuration failed : " + configuration)
    }
    done
  }

  private def init: Boolean = {
    onlyonce {
      factory.setConfiguration(configuration)
      connection = factory.createQueueConnection
      connection.start
    };
    true
  }

  private val factory = new QueueConnectionFactory
  private var connection: QueueConnection = null
  private val defaulttimeout: Long = 90000
  private val veryshortinterval = 20
  private val shortinterval = 200
  private val longinterval = 1000
  private val maxduration = new AtomicLong(0)

}
