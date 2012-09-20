package de.man.mn.gep.scala.config.enovia5.metadata.server.version

import java.sql.Timestamp
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper

import de.man.mn.gep.scala.config.enovia5.metadata.server.Conversions
import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

class VersionDetail(
  val id: String,
  val masterid: String,
  val row: Option[Int],
  val name: NString,
  val versionstring: NString,
  val changerequest: Option[NString],
  val description_de: Option[NString],
  val description_en: Option[NString],
  val description_fr: Option[NString],
  val description_pl: Option[NString],
  val description_tr: Option[NString],
  val material: Option[NString],
  val weight: Option[NString],
  statusstring: String,
  val owner: String,
  val team: Option[String],
  val project: Option[NString],
  lockstatus: Option[String],
  lockuser: Option[String],
  val creator: Option[String],
  val lastmodified: Timestamp,
  val created: Option[Timestamp],
  assembly: Int,
  standardpart: Option[String],
  val instances: Option[String] = None,
  val parts: Option[String] = None,
  val parents: Option[String] = None,
  val quantity: Option[Int],
  val level: Option[Int])(implicit databaserepresentation: DatabaseRepresentation)
  extends PropertiesMapper {

  val datatype = "versions"
  val lockowner = Conversions.lockowner(lockstatus, lockuser)
  val status = Conversions.status(statusstring)
  val isassembly = Conversions.isassembly(assembly)
  val isstandardpart = Conversions.isstandard(standardpart)
  val pdm = Conversions.origin(databaserepresentation.baseuri)
  val links = {
    val base = databaserepresentation.baseuri
    val url = if (base.contains("versions/"))
      base.substring(0, base.indexOf("versions/")) + "versions/" + id
    else
      base.substring(0, base.indexOf("products/")) + "versions/" + id

    List(
      ("version", url + "/"),
      ("bom", url + "/bom/?equal&id&*"),
      ("whereused", url + "/whereused/?equal&id&*"),
      ("instances", url + "/instances/?equal&id&*"),
      ("products", url + "/products/?equal&id&*"),
      ("snapshots", url.substring(0, url.indexOf("versions/")) + "snapshots/?equal&parent&0x" + id + "&sort&lastmodified"),
      ("partnerversions", url.substring(0, url.indexOf("versions/")) + "partnerversions/?equal&partnerpartname&0x" + id + "&sort&lastmodified"),
      ("formatssummary", url + "/formats/summary/"),
      ("formatsdetails", url + "/formats/details/"),
      ("assembly", url + "/assembly/E137A1C70221436FB881EE2773787EE2/"),
      ("iterations", url + "/iterations/"),
      ("owner", "/users/details/" + owner + "/"),
      ("lockowner", "/users/details/" + lockowner.getOrElse("null") + "/"),
      ("spacetree", url + "/graph/spacetree/0/"),
      ("millertree", url + "/graph/millertree/0/")).toMap
  }

}
