package de.man.mn.gep.scala.config.enovia5.metadata.server.instance
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper

import de.man.mn.gep.scala.config.enovia5.metadata.server.Conversions
import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

abstract class InstanceDetail(
  val id: String,
  val row: Option[Int],
  val instance: NString,
  val name: NString,
  val versionstring: NString,
  assembly: Int,
  val level: String,
  val versionoid: String,
  lockstatus: Option[String],
  lockuser: Option[String],
  shownoshow: Option[String],
  shownoshowparent: Option[String],
  project2d: Option[String],
  val product: NString,
  productoid: String,
  val parentname: Option[NString],
  val parentversion: Option[NString],
  val description_de: Option[NString],
  parentoid: Option[String])(implicit databaserepresentation: DatabaseRepresentation)
  extends PropertiesMapper {

  val datatype = "instances"
  val pdm = Conversions.origin(databaserepresentation.baseuri)
  val lockowner = Conversions.lockowner(lockstatus, lockuser)
  val isassembly = Conversions.isassembly(assembly)
  val ishidden = Conversions.ishidden(shownoshow)
  val ishiddenbyparent = Conversions.ishidden(shownoshowparent)
  val isproject2d = Conversions.isproject2d(project2d)
  val parent = parentname.getOrElse("no parent") + " " + parentversion.getOrElse("")
  val links = {
    val base = databaserepresentation.baseuri
    val url = if (base.contains("versions/"))
      base.substring(0, base.indexOf("versions/")) + "instances/" + id
    else
      base.substring(0, base.indexOf("instances/")) + "instances/" + id
    val versionurl = url.substring(0, url.indexOf("instances/")) + "versions/" + versionoid
    List(
      ("version", versionurl + "/"),
      ("bom", url + "/bom/?equal&id&*"),
      ("whereused", url + "/whereused/?equal&id&*"),
      ("versions", url.substring(0, url.indexOf("instances/")) + "versions/?equal&id&0x" + versionoid + "&sort&id"),
      ("parent", url.substring(0, url.indexOf("instances/")) + "versions/" + parentoid.getOrElse("0") + "/"),
      ("products", url.substring(0, url.indexOf("instances/")) + "products/?equal&id&0x" + productoid + "&sort&id"),
      ("snapshots", url.substring(0, url.indexOf("instances/")) + "snapshots/?equal&parent&0x" + id + "&sort&lastmodified"),
      ("lockowner", "/users/details/" + lockowner.getOrElse("null") + "/"),
      ("formatssummary", url + "/formats/summary/"),
      ("formatsdetails", url + "/formats/details/"),
      ("assembly", url + "/assembly/E137A1C70221436FB881EE2773787EE2/"),
      ("iterations", url + "/iterations/"),
      ("millertree", url + "/graph/millertree/0/"),
      ("spacetree", url + "/graph/spacetree/0/"),
      ("instance", url + "/")).toMap
  }

}
