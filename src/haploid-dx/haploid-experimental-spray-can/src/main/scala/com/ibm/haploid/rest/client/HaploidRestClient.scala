package com.ibm.haploid.rest.client

import com.ibm.haploid.rest.HaploidRestBase
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import cc.spray.can.client.HttpClient
import cc.spray.client.DispatchStrategies
import cc.spray.client.DispatchStrategy
import cc.spray.client.HttpConduit
import cc.spray.http.HttpCharsets._
import cc.spray.http.{ HttpContent, HttpHeader }
import sun.misc.BASE64Encoder
import cc.spray.io.pipelines.ClientSSLEngineProvider
import scala.util.Random
import com.ibm.haploid.core.concurrent.{  actorsystem ⇒ system }

trait HaploidRestClient extends HaploidRestBase with App {

  val httpClient = system.actorOf(
    props = Props(new HttpClient(ioWorker, ConfigFactory.parseString("spray.can.client.ssl-encryption = off"))),
    name = clientActorName)

  implicit def httpContentToString(content: HttpContent): String = {
    new String(content.buffer, content.contentType.charset.getOrElse(`UTF-8`).nioCharset)
  }

  def createConduit(
    host: String,
    port: Int = 80,
    dispatchStrategy: DispatchStrategy = DispatchStrategies.NonPipelined(),
    config: Config = ConfigFactory.load()): HttpConduit = {

    return new HttpConduit(httpClient, host, port, dispatchStrategy, config)(system)

  }

  def getBasicAuthentificationValue(username: String, password: String): String = {
    "Basic " + new BASE64Encoder().encode((username + ":" + password).getBytes())
  }

  def getBasicAuthentificationHeader(
    username: String,
    password: String)(implicit conduit: HttpConduit): HttpHeader = {

    val phrase = getBasicAuthentificationValue(username, password)
    val header = HttpHeader("Authorization", phrase)

    header
  }

  def getMultipartFormDataHeader: (String, HttpHeader) = {

    val boundary = "VFC-Plauen-" + Random.nextInt.toString
    val value = "multipart/form-data, boundary=" + boundary
    val header = HttpHeader("Content-type", value)

    (boundary, header)
  }

}