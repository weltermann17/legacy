package de.man.mn.gep.scala.config.enovia5.metadata.server.product

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper

import de.man.mn.gep.scala.config.enovia5.metadata.server.Conversions
import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

class ProductDetail(
  val id: String,
  val row: Option[Int],
  val name: NString,
  val description_de: Option[NString],
  val owner: String,
  val team: Option[String],
  val project: Option[NString],
  lockstatus: Option[String],
  lockuser: Option[String],
  val lastmodified: java.sql.Timestamp,
  val created: Option[java.sql.Timestamp],
  val instances: Option[String],
  val parts: Option[String],
  val masterid: Option[String] = None,
  val level: Option[Int])(implicit databaserepresentation: DatabaseRepresentation)
  extends PropertiesMapper {

  val datatype = "products"
  val lockowner = Conversions.lockowner(lockstatus, lockuser)
  val isassembly = true
  val pdm = Conversions.origin(databaserepresentation.baseuri)
  val links = {
    val base = databaserepresentation.baseuri
    val url = if (base.contains("versions/"))
      base.substring(0, base.indexOf("versions/")) + "products/" + id
    else
      base.substring(0, base.indexOf("products/")) + "products/" + id

    List(
      ("product", url + "/"),
      ("bom", url + "/bom/?equal&id&*"),
      ("formatssummary", url + "/formats/summary/"),
      ("formatsdetails", url + "/formats/details/"),
      ("assembly", url + "/assembly/E137A1C70221436FB881EE2773787EE2/"),
      ("iterations", url + "/iterations/"),
      ("snapshots", url.substring(0, url.indexOf("products/")) + "snapshots/?equal&parent&0x" + id + "&sort&lastmodified"),
      ("owner", "/users/details/" + owner + "/"),
      ("spacetree", url + "/graph/spacetree/0/"),
      ("millertree", url + "/graph/millertree/0/"),
      ("lockowner", "/users/details/" + lockowner.getOrElse("null") + "/")).toMap
  }
}
