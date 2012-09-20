package com.ibm.de.ebs.plm.scala.rest

import org.restlet.data.CharacterSet
import org.restlet.data.Language
import org.restlet.data.MediaType
import org.restlet.data.Status
import org.restlet.representation.StringRepresentation
import org.restlet.resource.Delete
import org.restlet.resource.Get
import org.restlet.resource.Options
import org.restlet.resource.Post
import org.restlet.resource.Put
import org.restlet.resource.Finder
import org.restlet.resource.ServerResource
import org.restlet.Context
import org.restlet.Request
import org.restlet.Response

import com.ibm.de.ebs.plm.scala.jms.Jms
import com.ibm.de.ebs.plm.scala.jms.JmsUsing

case class JmsResource(
  jms: Jms,
  queue: String,
  parameters: List[(String, String)],
  timeout: Long,
  context: Context) extends ServerResource with JmsUsing {

  @Get
  @Options
  @Post
  @Put
  @Delete
  def doRequest = new StringRepresentation(
    jms.request(queue, "", parameters, timeout) match {
      case Some(v) => v
      case None =>
        getResponse.setStatus(Status.SERVER_ERROR_GATEWAY_TIMEOUT)
        "{}"
    }, MediaType.APPLICATION_JSON, Language.ALL, CharacterSet.UTF_8)

}

case class JmsResourceFinder(uritemplate: String, jms: Jms, timeout: Long, context: Context)
  extends Finder(context) with CanQuery with HasParameters {
  /**
   * uritemplate, e.g. "/divisions/muc/subsystems/enovia5/vaultfiles/{vault}/local/{vaultfilepath}/{httpdfilepath}/"
   */
  override def create(request: Request, response: Response, parameters: Map[String, String]): ServerResource = {
    /**
     * TODO:  fix "computername"
     */
    val queue = if (uritemplate.contains("{computername}")) uritemplate.replace("{computername}", parameters.toMap.get("computername").get) else uritemplate
    JmsResource(jms, queue, List(("com.ibm.de.ebs.plm.scala.rest.http.method", request.getMethod.getName.toLowerCase)) ++ parameters, timeout, context)
  }
}

