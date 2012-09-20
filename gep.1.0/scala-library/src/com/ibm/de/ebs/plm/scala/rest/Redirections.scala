package com.ibm.de.ebs.plm.scala.rest

import org.restlet.data.MediaType
import org.restlet.data.Preference
import org.restlet.resource.Get
import org.restlet.resource.ClientResource
import org.restlet.resource.ServerResource
import org.restlet.routing.Redirector
import org.restlet.routing.Router
import org.restlet.Application
import org.restlet.Context
import org.restlet.Request
import org.restlet.Response
import org.restlet.Restlet

import com.ibm.de.ebs.plm.scala.json.Json

import Redirections.forceGzip

case class Redirections(redirections: Json.JObject, context: Context) extends Application(context) {

  getDecoderService.setEnabled(false)
  getTunnelService.setEnabled(false)

  override def createInboundRoot = {
    val router = new Router(context)
    redirections.foreach {
      case (from, to) =>
        router.attach(from, new Redirect(to.asString, context))
        ()
    }
    router
  }
}

case class Redirect(targettemplate: String, context: Context)
  extends org.restlet.routing.Redirector(context, targettemplate, Redirector.MODE_SERVER_OUTBOUND) {
  override def handle(request: Request, response: Response) = {
    request.getClientInfo.setAgent("GEPserver/1.0")
    super.handle(request, response)
    if (response.getStatus.isSuccess) {
      if ("pjpeg" == response.getEntity.getMediaType.getSubType) {
        response.getEntity.setMediaType(MediaType.IMAGE_JPEG)
      }
    }
  }
}

case class RedirectorResource(
  targettemplate: String,
  context: Context) extends ServerResource {

  @Get
  def doRequest = {
    Redirect(targettemplate, context).handle(getRequest, getResponse)
    getResponse.getEntity
  }
}

case class Forwarder(uri: String, context: Context) extends Restlet {

  override def handle(request: Request, response: Response): Unit = {
    val client = new ClientResource(context, uri)
    client.setResponse(response)
    client.setChallengeResponse(request.getChallengeResponse)
    client.setRetryAttempts(1)
    client.setRetryDelay(500)
    forceGzip(client.getRequest)
    response.setEntity(client.get)
  }
}

object Redirections {

  def forceGzip(request: Request) = {
    val acceptedencodings = request.getClientInfo.getAcceptedEncodings
    acceptedencodings.clear
    acceptedencodings.add(new Preference(org.restlet.data.Encoding.GZIP, 1.f))
  }
}
