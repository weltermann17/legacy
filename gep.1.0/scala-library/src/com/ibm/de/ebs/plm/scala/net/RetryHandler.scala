package com.ibm.de.ebs.plm.scala.net

import org.apache.http.protocol.ExecutionContext
import org.apache.http.protocol.HttpContext
import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.HttpRequest
import org.apache.http.NoHttpResponseException

class RetryHandler extends org.apache.http.client.HttpRequestRetryHandler {

  override def retryRequest(e: java.io.IOException, executioncount: Int, context: HttpContext): Boolean = {
    println("in HttpRequestRetryHandler: " + executioncount + " " + e)
    try {
      if (2 < executioncount) {
        false
      } else {
        e match {
          case _: NoHttpResponseException => true
          case _ =>
            val request = context.getAttribute(ExecutionContext.HTTP_REQUEST).asInstanceOf[HttpRequest]
            if (!request.isInstanceOf[HttpEntityEnclosingRequest]) true else false
        }
      }
    } catch {
      case e => e.printStackTrace; true
    }
  }
}
