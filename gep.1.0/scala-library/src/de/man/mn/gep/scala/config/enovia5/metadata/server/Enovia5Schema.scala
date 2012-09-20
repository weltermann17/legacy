package de.man.mn.gep.scala.config.enovia5.metadata.server

import org.squeryl.annotations._
import org.squeryl.KeyedEntity
import org.squeryl.Schema

case class ProductRootClass(
  @Column("VID") name: String,
  @Column("VNAME") description: Option[String],
  @Column("OID") id: String) {

  def this() = this("", Some(""), "")
}

case class PartMaster(
  @Column("VID") name: String,
  @Column("V511PARTTYPE") parttype: Int,
  @Column("OID") id: String) {

  @Transient val isassembly = Conversions.isassembly(parttype)
}

case class PartVersion(
  @Column("VVERSION") versionstring: String,
  @Column("VSTATUS") status: String,
  @Column("VUSER") owner: String,
  @Column("LOCKSTATUS") lockstatus: Option[String],
  @Column("LOCKUSER") lockuser: Option[String],
  @Column("CMODIFIED") lastmodified: java.sql.Date,
  @Column("MNECNUM") changerequest: Option[String],
  @Column("MNDESCDE") description_de: Option[String],
  @Column("MNPARTSTD") standardpart: Option[String],
  @Column("VMASTER") master: String,
  @Column("OID") id: String) {

  def this() = this("", "", "", Some(""), Some(""), null, Some(""), Some(""), Some(""), "", "")

  @Transient val lockowner = Conversions.lockowner(lockstatus, lockuser)
  @Transient val isstandardpart = Conversions.isstandard(standardpart)

}

object Enovia5Schema extends Schema {

  val products = table[ProductRootClass]("ENOVIA.MANPRODUCTROOTCLASS")
  val partmasters = table[PartMaster]("ENOVIA.MANPARTMASTER")
  val partversions = table[PartVersion]("ENOVIA.MANPARTVERSION")

  case class Version(
    row: Int,
    baseuri: String,
    master: PartMaster,
    version: PartVersion) {

    val datatype = "versions"

    val links = {
      val url = baseuri + version.id
      List(
        ("version", url + "/"),
        ("bom", url + "/bom/?equal&id&*"),
        ("whereused", url + "/whereused/?equal&id&*"),
        ("instances", url + "/instances/?equal&id&*"),
        ("partners", url + "/partners/?equal&id&*"),
        ("formatssummary", url + "/formats/summary/"),
        ("owner", "/users/details/" + version.owner + "/"),
        ("lockowner", "/users/details/" + version.lockowner.getOrElse("null") + "/"),
        ("spacetree", url + "/graph/spacetree/0/"),
        ("millertree", url + "/graph/millertree/0/")).toMap
    }

    def toMap = Map(
      "links" -> links,
      "name" -> master.name,
      "versionstring" -> version.versionstring,
      "changerequest" -> version.changerequest,
      "description_de" -> version.description_de,
      "status" -> version.status,
      "owner" -> version.owner,
      "lockowner" -> version.lockowner,
      "lastmodified" -> version.lastmodified,
      "isassembly" -> master.isassembly,
      "isstandardpart" -> version.isstandardpart,
      "id" -> version.id)

    override def toString = com.ibm.de.ebs.plm.scala.json.Json.build(toMap)

  }

}

