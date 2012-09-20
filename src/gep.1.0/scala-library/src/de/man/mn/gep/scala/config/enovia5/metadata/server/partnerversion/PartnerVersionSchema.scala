package de.man.mn.gep.scala.config.enovia5.metadata.server.partnerversion

import java.sql.Timestamp
import org.squeryl.annotations._
import org.squeryl.PrimitiveTypeMode.optionString2ScalarString
import org.squeryl.PrimitiveTypeMode.string2ScalarString
import org.squeryl.PrimitiveTypeMode.timestamp2ScalarTimestamp
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import com.ibm.de.ebs.plm.scala.util.Converters
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper
import com.ibm.de.ebs.plm.scala.text.Uuid
import com.ibm.de.ebs.plm.scala.text.Uuid.uuid2string
import de.man.mn.gep.scala.config.enovia5.metadata.server.Conversions
import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.partner.Partners
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.Versions
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.ArrayColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.CompressedColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.FunctionColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.UniqueColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTableFiller
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.LinkTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.partner.PartnerMappings
import com.ibm.de.ebs.plm.scala.database.Raw

class PartnerVersionTable(
  @Column(name = "OID", length = 32) val id: String = Uuid.newType4Uuid.toUpperCase,
  @Column(name = "VID", length = 80) var name: String,
  @Column(name = "CCREATED") val created: Timestamp = new Timestamp(System.currentTimeMillis),
  @Column(name = "CMODIFIED") var lastmodified: Timestamp = new Timestamp(System.currentTimeMillis),
  @Column(name = "LOCKSTATUS", length = 1) var lockstatus: Option[String] = None,
  @Column(name = "LOCKUSER", length = 32) var lockuser: Option[String] = None,
  @Column(name = "VUSER", length = 32) var owner: String,
  @Column(name = "VORGANIZATION", length = 32) var team: String,
  @Column(name = "VPROJECT", length = 32) var project: String,
  @Column(name = "VMODIFIER", length = 32) var modifier: Option[String] = None,
  @Column(name = "VMODIFIERORGANIZATION", length = 32) var modifierteam: Option[String] = None,
  @Column(name = "VMODIFIERPROJECT", length = 32) var modifierproject: Option[String] = None,
  @Column(name = "VNAME", length = 250) var alternatename: Option[String] = None,
  @Column(name = "VDESCRIPTION", length = 250) var description_de: Option[String] = None,
  @Column(name = "VVERSION", length = 40) var versionstring: Option[String] = None,
  @Column(name = "VREVISION", length = 40) var revisionstring: Option[String] = None,
  @Column(name = "VSTATUS", length = 80) var statusstring: Option[String] = None,
  @Column(name = "VCHANGEREQUEST", length = 80) var changerequest: Option[String] = None,
  @Column(name = "VCHANGEREQUESTPARTNER", length = 80) var changerequestpartner: Option[String] = None,
  @Column(name = "VRELEASEDATE") var releasedate: Option[Timestamp] = None,
  @Column(name = "VVALIDATIONCODE", length = 80) var validationcode: Option[String] = None,
  @Column(name = "VVALIDATIONCODEPARTNER", length = 80) var validationcodepartner: Option[String] = None,
  @Column(name = "VPARTNER", length = 32) val partner: String,
  @Column(name = "VPARTMASTER", length = 32) val master: String,
  @Column(name = "VPARTVERSION", length = 32) val version: String,
  @Column(name = "VCADTYPE", length = 40) var cadtype: Option[String] = None,
  @Column(name = "VPDA3D", length = 40) var pda3d: Option[String] = None,
  @Column(name = "VDATA") val data: Option[Array[Byte]] = None)

  extends KeyedEntity[String] {

  def update = {
    lastmodified = new Timestamp(System.currentTimeMillis)
    PartnerVersionSchema.partnerversions.update(this)
  }

}

class PartnerVersionDetail(

  val row: Option[Int] = None,
  val name: String,
  val alternatename: Option[String] = None,
  val description_de: Option[String] = None,
  val owner: String,
  lockstatus: Option[String] = None,
  lockuser: Option[String] = None,
  val lastmodified: Timestamp,
  val partner: String,
  val master: String,
  val version: String,
  val id: String,
  val team: String,
  val project: String,
  val versionstring: Option[String] = None,
  val cadtype: Option[String] = None,
  val statusstring: Option[String] = None,
  val revisionstring: Option[String] = None,
  val changerequest: Option[String] = None,
  val changerequestpartner: Option[String] = None,
  val releasedate: Option[Timestamp] = None,
  val validationcode: Option[String] = None,
  val validationcodepartner: Option[String] = None,
  val pda3d: Option[String] = None,
  val created: Option[Timestamp] = None,
  val modifier: Option[String] = None,
  val modifierteam: Option[String] = None,
  val modifierproject: Option[String] = None)(implicit databaserepresentation: DatabaseRepresentation, parameters: Map[String, String])

  extends PropertiesMapper {

  def this(s: PartnerVersionTable)(implicit databaserepresentation: DatabaseRepresentation, parameters: Map[String, String]) = {

    this(
      name = s.name,
      owner = s.owner,
      partner = s.partner,
      master = s.master,
      version = s.version,
      id = s.id,
      lastmodified = s.lastmodified,
      description_de = s.description_de,
      alternatename = s.alternatename,
      project = s.project,
      changerequest = s.changerequest,
      changerequestpartner = s.changerequestpartner,
      team = s.team,
      lockstatus = s.lockstatus,
      lockuser = s.lockuser,
      validationcode = s.validationcode,
      validationcodepartner = s.validationcodepartner,
      pda3d = s.pda3d,
      modifier = s.modifier,
      modifierteam = s.modifierteam,
      modifierproject = s.modifierproject,
      statusstring = s.statusstring,
      revisionstring = s.revisionstring,
      versionstring = s.versionstring,
      cadtype = s.cadtype,
      releasedate = s.releasedate)(databaserepresentation, parameters)
  }

  val datatype = "partnerversions"
  val isassembly = true

  implicit private val _ = databaserepresentation.connectionfactory
  private def versionmappings = Repository(classOf[Versions])
  lazy val versionname = {
    val versionid = versionmappings(Raw(version))
    versionmappings.name(versionid) + " " + versionmappings.versionstring(versionid)
  }

  private def partnermappings = Repository(classOf[Partners])
  lazy val partnername = partnermappings.name(partnermappings(Raw(partner)))

  lazy val lockowner = Conversions.lockowner(lockstatus, lockuser)
  lazy val links = {
    val base = databaserepresentation.baseuri
    val url = base.substring(0, base.indexOf("partnerversions/")) + "partnerversions/" + id
    List(
      ("partnerversion", url + "/"),
      ("formatsdetails", url + "/formats/details/"),
      ("owner", "/users/details/" + owner + "/"),
      ("lockowner", "/users/details/" + lockowner.getOrElse("") + "/")).toMap
  }

}

object PartnerVersionSchema extends Schema {

  val partnerversions = table[PartnerVersionTable]("ENOREAD.MANPARTNERVERSION")

  on(partnerversions)(s => declare(
    s.master is (dbType("raw(16)")),
    s.version is (dbType("raw(16)")),
    s.partner is (dbType("raw(16)")),
    s.description_de is (indexed("ENOREAD.MANPARTNERVERSIONI01")),
    s.lastmodified is (indexed("ENOREAD.MANPARTNERVERSIONI02")),
    s.owner is (indexed("ENOREAD.MANPARTNERVERSIONI03")),
    s.project is (indexed("ENOREAD.MANPARTNERVERSIONI04")),
    s.name is (indexed("ENOREAD.MANPARTNERVERSIONI05")),
    s.alternatename is (indexed("ENOREAD.MANPARTNERVERSIONI06")),
    columns(s.partner, s.master) are (indexed("ENOREAD.MANPARTNERVERSIONI07"), unique),
    s.id is (primaryKey, dbType("raw(16)"))))
}

