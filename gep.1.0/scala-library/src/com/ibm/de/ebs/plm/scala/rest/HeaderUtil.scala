package com.ibm.de.ebs.plm.scala.rest

import org.restlet.util.Series
import org.restlet.engine.header.HeaderConstants.ATTRIBUTE_HEADERS
import org.restlet.engine.header.Header
import org.restlet.Message

object HeaderUtils {

  def dumpHeaders(message: Message) = {
    headers(message).toString
  }

  def hasCustomHeader(message: Message, name: String) = {
    headers(message).getNames.contains(name)
  }

  def getCustomHeader(message: Message, name: String) = {
    val h = headers(message)
    if (h.getNames.contains(name)) h.getFirstValue(name) else null
  }

  def setCustomHeader(message: Message, name: String, value: String) = {
    headers(message).set(name, value)
  }

  def renameHeader(message: Message, from: String, to: String) = {
    val h = headers(message)
    if (h.getNames.contains(from)) {
      h.set(to, h.getFirstValue(from))
      h.removeFirst(from)
      true
    } else {
      false
    }
  }

  def removeHeader(message: Message, name: String) = {
    val h = headers(message)
    if (h.getNames.contains(name)) {
      h.removeFirst(name)
      true
    } else {
      false
    }
  }

  private def headers(message: Message) = {
    var h = message.getAttributes.get(ATTRIBUTE_HEADERS).asInstanceOf[Series[Header]]
    if (null == h) {
      h = new Series(classOf[Header])
      message.getAttributes.put(ATTRIBUTE_HEADERS, h)
    }
    h
  }
}