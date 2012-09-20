package de.man.mn.gep.scala.config.enovia5.metadata

import org.restlet.resource._
import org.restlet.routing._
import org.restlet._
import com.ibm.de.ebs.plm.scala.database._
import com.ibm.de.ebs.plm.scala.json.JsonConversions._
import com.ibm.de.ebs.plm.scala.rest._
import de.man.mn.gep.scala.Server
import server.shared._
import server.version._
import server.instance._
import server.product._
import server.partnerversion._
import server.snapshot._
import server._
import inmemory.product.{ ProductInMemory, AssemblyInMemory, VersionInMemory, BomInMemory, WhereUsedInMemory }
import inmemory.document.{ FormatsSummaryInMemory, FormatsDetailsInMemory, IterationsInMemory }
import de.man.mn.gep.scala.config.enovia5.metadata.server.version._

class MetadataServer {

  def apply(
    baseuri: String,
    connectionfactory: ConnectionFactory,
    router: Router) = {

    val context = Server.childContext

    case class Enovia5OracleQueryFilter(next: Restlet with CanQuery, context: Context)
      extends OracleQueryFilter(next, context) {

      override def mapColumns(name: String): String = {
        name match {
          case "id" => "a.oid"
          case "vpv" => "b.oid"
          case "parent" => "a.vparent"
          case "parenttype" => "a.vparenttype"
          case "partname" => "b.vid"
          case "productname" => "upper(a.vid)"
          case "snapshotname" => "upper(a.vname)"
          case "partnername" => "a.vid"
          case "partnerpartnumber" => "a.vid"
          case "partnerpartname" => "p.vid"
          case "partnerpartdescription" => "upper(a.vdescription)"
          case "partversion" => "a.vpartversion"
          case "expires" => "cexpirationdate"
          case "partdescription" => "upper(a.mndescde||a.mndescen||a.mndescfr||a.mndescpl||a.mndesctr)"
          case "productdescription" => "upper(a.vname||a.vdescription)"
          case "version" => "a.vversion"
          case "owner" => "a.vuser"
          case "lastmodified" => "a.cmodified"
          case "lockedby" => "a.lockstatus='Y' and a.lockuser"
          case "eco" => "a.mnecnum"
          case "partproject" => "upper(a.vproject0020)"
          case "productproject" => "upper(a.vproject0004)"
          case n => throw new IndexOutOfBoundsException("Enovia5QueryFilter : no mapping for '" + n + "'")
        }
      }
    }

    case class Enovia5ResourceFinder[D <: DatabaseRepresentation](
      uritemplate: String,
      context: Context,
      representationclass: Class[D])
      extends Finder(context) with CanQuery with HasParameters {

      override def parameternames = super.parameternames ++ queryparameternames.toList

      override def create(request: Request, response: Response, parameters: Map[String, String]): ServerResource = {
        DatabaseResource(baseuri + uritemplate, parameters ++ Map("authorization-identifier" -> request.getChallengeResponse.getIdentifier.toUpperCase), connectionfactory, representationclass)
      }
    }

    def Enovia5DatabaseResource[D <: DatabaseRepresentation](
      parameters: List[String],
      uri: String,
      representationclass: Class[D]) = {

      com.ibm.de.ebs.plm.scala.rest.Encoding(
        Enovia5OracleQueryFilter(
          Enovia5ResourceFinder(uri, context, representationclass),
          context),
        context)
    }

    object Enovia5Metadata extends UriBuilder {
      "/divisions/<division>" -> "truck" + "engine"
      "/subsystems/<subsystem>" -> "enovia5" -->
        "/versions/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[Versions]) } +
        "/versions/{version}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[VersionInMemory]) } +
        "/versions/{version}/bom/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[server.version.Bom]) } +
        "/versions/{version}/whereused/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[WhereUsed]) } +
        "/versions/{version}/instances/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[Instances]) } +
        "/versions/{version}/partners/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[Partners]) } +
        "/versions/{version}/iterations/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[IterationsInMemory]) } +
        "/versions/{version}/formats/details/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[FormatsDetailsInMemory]) } +
        "/versions/{version}/formats/summary/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[FormatsSummaryInMemory]) } +
        "/versions/{version}/assembly/{partner}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[AssemblyInMemory]) } +
        "/versions/{version}/products/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[VersionProducts]) } +
        "/versions/{version}/graph/spacetree/{node}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[server.version.SpaceTree]) } +
        "/versions/{version}/graph/millertree/{node}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[server.version.MillerTree]) } +
        "/instances/{instance}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[Instance]) } +
        "/instances/{instance}/assembly/{partner}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[AssemblyInMemory]) } +
        "/instances/{instance}/formats/details/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[FormatsDetailsInMemory]) } +
        "/instances/{instance}/formats/summary/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[FormatsSummaryInMemory]) } +
        "/partners/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[Partners]) } +
        "/products/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[Products]) } +
        "/products/{product}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[ProductInMemory]) } +
        "/products/{product}/bom/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[server.product.Bom]) } +
        "/products/{product}/graph/spacetree/{node}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[server.product.SpaceTree]) } +
        "/products/{product}/graph/millertree/{node}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[server.product.MillerTree]) } +
        "/products/{product}/assembly/{partner}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[AssemblyInMemory]) } +
        "/products/{product}/iterations/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[IterationsInMemory]) } +
        "/products/{product}/formats/details/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[FormatsDetailsInMemory]) } +
        "/products/{product}/formats/summary/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[FormatsSummaryInMemory]) } +
        "/partnerversions/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[PartnerVersions]) } +
        "/partnerversions/{partnerversion}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[PartnerVersion]) } +
        "/snapshots/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[Snapshots]) } +
        "/snapshots/{snapshot}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[Snapshot]) } +
        "/snapshots/{snapshot}/assembly/{partner}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[SnapshotBlobs]) } +
        "/snapshots/{snapshot}/bom/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[SnapshotBlobs]) } +
        "/snapshots/{snapshot}/whereused/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[SnapshotBlobs]) } +
        "/snapshots/{snapshot}/graph/spacetree/{node}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[SnapshotBlobs]) } +
        "/snapshots/{snapshot}/graph/millertree/{node}/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[SnapshotBlobs]) } +
        "/snapshots/{snapshot}/formats/details/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[SnapshotBlobs]) } +
        "/snapshots/{snapshot}/formats/summary/" --> { params => uri => Enovia5DatabaseResource(params, uri, classOf[SnapshotBlobs]) }
    }

    Enovia5Metadata.attach(router)
  }
}
