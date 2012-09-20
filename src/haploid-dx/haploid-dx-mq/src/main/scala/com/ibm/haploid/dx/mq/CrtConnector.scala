package com.ibm.haploid

package dx

package mq

import com.ibm.haploid.core.newLogger
import com.ibm.mq.constants.CMQC
import com.ibm.mq.{ MQQueueManager, MQQueue, MQPutMessageOptions, MQMessage, MQGetMessageOptions, MQException, MQEnvironment }

/**
 * An example library class
 */

class CrtConnector {

  def isbrokeractive = {

    try {
      MQEnvironment.userID = user
      MQEnvironment.hostname = hostname
      MQEnvironment.channel = channel

      val queueManager = new MQQueueManager(manager)
      queueManager.disconnect
      true

    } catch {
      case (e: MQException) ⇒ {
        false
      }
    }
  }

  def sendRequest(message: String): String = {
    var queueManager: MQQueueManager = null
    var requestqueue: MQQueue = null
    var replyqueue: MQQueue = null

    var result = ""

    try {
      MQEnvironment.userID = user
      MQEnvironment.hostname = hostname
      MQEnvironment.channel = channel

      var running = true

      queueManager = new MQQueueManager(manager)

      requestqueue = queueManager.accessQueue(requestqueuename, CMQC.MQOO_OUTPUT)

      val request = new MQMessage

      request.writeString(message)

      val pmo = new MQPutMessageOptions
      pmo.options += CMQC.MQPMO_ASYNC_RESPONSE

      requestqueue.put(request, pmo)

      val msgId = request.messageId

      val gmo = new MQGetMessageOptions
      gmo.matchOptions = CMQC.MQMO_MATCH_CORREL_ID
      gmo.options = CMQC.MQGMO_WAIT

      val replyqueue = queueManager.accessQueue(replyqueuename, CMQC.MQOO_INPUT_SHARED)

      val reply = new MQMessage

      reply.correlationId = msgId
      gmo.waitInterval = timeout

      replyqueue.get(reply, gmo)

      if (reply != null) {

        val length = reply.getMessageLength
        val data = new Array[Byte](length)

        reply.readFully(data);
        
        result = new String(data, "UTF-8")
        logger.debug("MQ response : " + result)
      }

      result
    } catch {
      case (e: MQException) ⇒ {
        val errmsg = "MQ connection error : " + e.getMessage + "host=" + hostname + ":" + port + "; channel=" + channel + "; request-queue=" + requestqueuename +
          "request=" + message
        throw new MQException(errmsg, e.getErrorCode, e.reasonCode, e.completionCode)
      }
      case e @ _ ⇒ {
        throw new Exception(e)
      }
    } finally {
      if (requestqueue != null) requestqueue.close
      if (replyqueue != null) replyqueue.close
      if (queueManager != null) queueManager.disconnect
    }
  }

  private val logger = newLogger(this)
  
  logger.debug("CRT client; host= " + hostname + "; port= " + replyqueuename + "; user= " + user + "; queue manager= " + manager + "; channel= " + channel + "; request queue= " + requestqueuename + "; reply queue= " + replyqueuename + "; timeout= " + timeout)

}

