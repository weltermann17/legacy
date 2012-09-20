package de.man.mn.gep.scala.config.enovia5.metadata

import org.restlet._
import org.restlet.data._
import org.restlet.routing._
import org.restlet.resource._
import org.restlet.representation._
import com.ibm.de.ebs.plm.scala.util.Timers._
import com.ibm.de.ebs.plm.scala.rest._
import com.ibm.de.ebs.plm.scala.rest.Redirections._
import com.ibm.de.ebs.plm.scala.json._
import com.ibm.de.ebs.plm.scala.json.JsonConversions._
import de.man.mn.gep.scala.config.enovia5.catia5._
import de.man.mn.gep.scala.Server
import de.man.mn.gep.scala.config.enovia5.catia5.DerivedFormatFinder
import com.ibm.de.ebs.plm.scala.rest.Expires

class MetadataClient {

  def apply(
    directory: RepresentationDirectoryCache,
    router: Router) = {

    val context = Server.childContext

    case class Enovia5Client(hosturi: String, context: Context) extends Restlet {

      override def handle(request: Request, response: Response): Unit = {
        val hostref = request.getHostRef.toString
        val resourceuri = request.getResourceRef.toString
        val client = new ClientResource(context, resourceuri.replace(hostref, hosturi))
        forceGzip(client.getRequest)
        client.setResponse(response)
        client.setChallengeResponse(request.getChallengeResponse)
        client.setRetryAttempts(0)
        client.setRetryDelay(100)
        client.setClientInfo(request.getClientInfo)
        client.getRequest.setEntity(request.getEntity)
        client.setMethod(request.getMethod)
        response.setEntity(client.handle)
      }
    }

    def Enovia5ClientFinder(parameters: List[String], uri: String) = {

      val maxage = 1 second

      val division = parameters(0)
      val enovia5host = {
        val host = Server.get("de.man.mn.gep.enovia5.database.configuration").asObject(division).asString

        if ("truck".equals(division)) {
          val truck = try { "http://" + Server.get("de.man.mn.gep.enovia5.database.configuration.truck").asString }
          catch { case _ => host }
          truck
        } else if ("engine".equals(division)) {
          val engine = try { "http://" + Server.get("de.man.mn.gep.enovia5.database.configuration.engine").asString }
          catch { case _ => host }
          engine
        } else host
      }
      Expires(
        CachingRepresentations(
          directory,
          Enovia5Client(enovia5host, context),
          maxage,
          context),
        maxage,
        context)
    }

    object Enovia5Metadata extends UriBuilder {
      "/divisions/<division>" -> "truck" + "engine"

      "/subsystems/<subsystem>" -> "enovia5" -->
        "/versions/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/versions/{version}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/versions/{version}/bom/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/versions/{version}/whereused/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/versions/{version}/instances/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/versions/{version}/partners/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/versions/{version}/iterations/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/versions/{version}/formats/details/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/versions/{version}/formats/summary/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/versions/{version}/assembly/{partner}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/versions/{version}/products/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/versions/{version}/graph/spacetree/{node}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/versions/{version}/graph/millertree/{node}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/versions/{version}/derivedformats/{derivedformat}/{nicename}/{partner}/{prefix}/" --> { params => uri => DerivedFormatFinder(uri) } +
        "/instances/{instance}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/instances/{instance}/assembly/{partner}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/instances/{instance}/formats/details/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/instances/{instance}/formats/summary/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/instances/{instance}/derivedformats/{derivedformat}/{nicename}/{partner}/{prefix}/" --> { params => uri => DerivedFormatFinder(uri) } +
        "/catia5/backbone/{computername}/loadobject/{identifier}/" --> { params => uri => null } +
        "/catia5/backbone/{computername}/signon/{connectstring}/{username}/{password}/" --> { params => uri => null } +
        "/products/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/products/{product}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/products/{product}/bom/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/products/{product}/graph/spacetree/{node}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/products/{product}/graph/millertree/{node}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/products/{product}/assembly/{partner}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/products/{product}/iterations/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/products/{product}/formats/details/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/products/{product}/formats/summary/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/products/{product}/derivedformats/{derivedformat}/{nicename}/{partner}/{prefix}/" --> { params => uri => DerivedFormatFinder(uri) } +
        "/partnerversions/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/partnerversions/{partnerversion}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/snapshots/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/snapshots/{snapshot}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/snapshots/{snapshot}/assembly/{partner}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/snapshots/{snapshot}/bom/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/snapshots/{snapshot}/whereused/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/snapshots/{snapshot}/graph/spacetree/{node}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/snapshots/{snapshot}/graph/millertree/{node}/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/snapshots/{snapshot}/formats/details/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/snapshots/{snapshot}/formats/summary/" --> { params => uri => Enovia5ClientFinder(params, uri) } +
        "/snapshots/{snapshot}/derivedformats/{derivedformat}/{nicename}/{partner}/{prefix}/" --> { params => uri => DerivedFormatFinder(uri) }
    }

    Enovia5Metadata.attach(router)
  }
}
