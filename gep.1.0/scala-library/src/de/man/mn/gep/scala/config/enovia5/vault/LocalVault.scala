package de.man.mn.gep.scala.config.enovia5.vault

import org.restlet.data.Encoding
import org.restlet.data.MediaType
import org.restlet.data.Preference
import org.restlet.data.Reference
import org.restlet.data.Status
import org.restlet.resource.Get
import org.restlet.resource.ServerResource
import org.restlet.routing.Filter
import org.restlet.Request
import org.restlet.Response
import org.restlet.Restlet
import collection.JavaConversions._
import com.ibm.de.ebs.plm.scala.rest.CachingRepresentations
import com.ibm.de.ebs.plm.scala.rest.Forwarder
import com.ibm.de.ebs.plm.scala.rest.RepresentationDirectoryCache
import com.ibm.de.ebs.plm.scala.text.StringConversions.fromHexString
import com.ibm.de.ebs.plm.scala.util.Io.addDisposition
import com.ibm.de.ebs.plm.scala.util.Timers.Int2Unit
import de.man.mn.gep.scala.Server
import com.ibm.de.ebs.plm.scala.rest.Forwarder

case class CachingForwarderResource(
  directory: RepresentationDirectoryCache,
  uri: String,
  parameters: Map[String, String]) extends ServerResource {

  @Get
  def doRequest = {
    val request = getRequest
    val response = getResponse
    new CachingRepresentations(directory, Forwarder(uri, Server.childContext), 1 year, Server.childContext) {

      override def beforeHandle(request: Request, response: Response) = {
        super.beforeHandle(request, response) match {
          case e @ Filter.STOP =>
            addDisposition(response.getEntity, parameters, "nativeformat")
            e
          case e => e
        }
      }

      override def afterHandle(request: Request, response: Response) = {
        super.afterHandle(request, response)
        if (response.getStatus.isSuccess) {
          response.getCacheDirectives.clear;
          val representation = response.getEntity
          representation.setLanguages(null);
          representation.setLocationRef(null.asInstanceOf[Reference]);
          representation.getMediaType.getMainType match {
            case "image" =>
              representation.getMediaType.getSubType match {
                case "pjpeg" => representation.setMediaType(MediaType.IMAGE_JPEG)
                case "x-png" => representation.setMediaType(MediaType.IMAGE_PNG)
                case _ =>
              }
            case _ =>
          }
          addDisposition(representation, parameters, "nativeformat")
        } else if (999 < response.getStatus.getCode) {
          response.setStatus(Status.SERVER_ERROR_GATEWAY_TIMEOUT)
        }
      }
    }.handle(request, response)

    response.getEntity
  }
}

case class LocalVaultResource(files: Restlet, parameters: Map[String, String]) extends ServerResource {

  @Get
  def doRequest = {
    val request = getRequest
    val response = getResponse
    val reference = request.getResourceRef
    val serverfile = fromHexString(parameters("serverfile"))
    reference.setPath(reference.getPath + serverfile)
    Encoder.handle(request, response)
    addDisposition(response.getEntity, parameters, "nativeformat")
  }

  object Encoder
    extends com.ibm.de.ebs.plm.scala.rest.Encoding(files, Server.childContext) {

    override def beforeHandle(request: Request, response: Response) = {
      val isbrowser = {
        val agent = request.getClientInfo.getAgent.toLowerCase
        agent.contains("gecko") || agent.contains("firefox") || agent.contains("mozilla") || agent.contains("msie")
      }
      val acceptedencodings = request.getClientInfo.getAcceptedEncodings
      import org.restlet.data.Encoding
      val gzip = !isbrowser && acceptedencodings.exists { e: Preference[Encoding] => Encoding.GZIP.equals(e.getMetadata) }
      if (gzip) {
        acceptedencodings.clear
        acceptedencodings.add(new Preference(Encoding.GZIP, 1.f))
      } else {
        acceptedencodings.clear
        acceptedencodings.add(new Preference(Encoding.IDENTITY, 1.f))
      }
      Filter.CONTINUE
    }
  }
}

