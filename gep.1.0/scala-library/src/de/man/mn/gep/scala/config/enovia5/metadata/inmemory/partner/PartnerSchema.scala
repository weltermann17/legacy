package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.partner

import java.sql.Timestamp

import org.squeryl.PrimitiveTypeMode.optionString2ScalarString
import org.squeryl.PrimitiveTypeMode.string2ScalarString
import org.squeryl.PrimitiveTypeMode.timestamp2ScalarTimestamp
import org.squeryl.annotations.Transient
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.annotations._

import com.ibm.de.ebs.plm.scala.util.Converters

import com.ibm.de.ebs.plm.scala.database.PropertiesMapper
import com.ibm.de.ebs.plm.scala.text.Uuid.uuid2string
import com.ibm.de.ebs.plm.scala.text.Uuid

import de.man.mn.gep.scala.config.enovia5.metadata.server.Conversions
import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

class PartnerTable(
  @Column(name = "OID", length = 32) val id: String = Uuid.newType4Uuid.toUpperCase,
  @Column(name = "CCREATED") val created: Timestamp = new Timestamp(System.currentTimeMillis),
  @Column(name = "CMODIFIED") var lastmodified: Timestamp = new Timestamp(System.currentTimeMillis),
  @Column(name = "LOCKSTATUS", length = 1) var lockstatus: Option[String] = None,
  @Column(name = "LOCKUSER", length = 32) var lockuser: Option[String] = None,
  @Column(name = "VUSER", length = 32) var owner: String,
  @Column(name = "VORGANIZATION", length = 32) var team: String,
  @Column(name = "VPROJECT", length = 32) var project: String,
  @Column(name = "VID", length = 80) var name: String,
  @Column(name = "VNAME", length = 250) var alternatename: String,
  @Column(name = "VDESCRIPTION", length = 250) var description_de: Option[String] = None)

  extends KeyedEntity[String] {

  def update = {
    lastmodified = new Timestamp(System.currentTimeMillis)
    PartnerSchema.partners.update(this)
  }

}

class PartnerDetail(
  val id: String,
  val row: Option[Int],
  val name: String,
  val alternatename: String,
  val description_de: Option[String],
  val created: Option[Timestamp],
  val lastmodified: Timestamp,
  val owner: String,
  val team: Option[String],
  val project: Option[String],
  lockstatus: Option[String],
  lockuser: Option[String])(implicit databaserepresentation: DatabaseRepresentation)

  extends PropertiesMapper {

  def this(t: PartnerTable)(implicit databaserepresentation: DatabaseRepresentation) = {
    this(
      t.id,
      None,
      t.name,
      t.alternatename,
      t.description_de,
      Some(t.created),
      t.lastmodified,
      t.owner,
      Some(t.team),
      Some(t.project),
      t.lockstatus,
      t.lockuser)(databaserepresentation)
  }

  val datatype = "partners"
  val isassembly = false
  lazy val lockowner = Conversions.lockowner(lockstatus, lockuser)

}

class PartnerMapping(
  @Column(name = "VPARTNER", length = 32) val partner: String,
  @Column(name = "VMAPPING", length = 40) val mapping: String,
  @Column(name = "VFROMTABLE", length = 40) val fromtable: Option[String] = None,
  @Column(name = "VFROMCOLUMN", length = 40) val fromcolumn: Option[String] = None,
  @Column(name = "VATTRIBUTE", length = 80) val attribute: Option[String] = None,
  @Column(name = "VPROPERTY", length = 80) val property: Option[String] = None,
  @Column(name = "VACTION", length = 40) val action: Option[String] = None,
  @Column(name = "VDESCRIPTION", length = 250) val description_de: Option[String] = None)

object PartnerSchema extends Schema {

  val partners = table[PartnerTable]("ENOREAD.MANPARTNER")
  val partnermappings = table[PartnerMapping]("ENOREAD.MANPARTNERMAPPING")

  on(partners)(s => declare(
    s.description_de is (indexed("ENOREAD.MANPARTNERI01")),
    s.name is (indexed("ENOREAD.MANPARTNERI02"), unique),
    s.lastmodified is (indexed("ENOREAD.MANPARTNERI03")),
    s.owner is (indexed("ENOREAD.MANPARTNERI04")),
    s.project is (indexed("ENOREAD.MANPARTNERI05")),
    s.alternatename is (indexed("ENOREAD.MANPARTNERI06")),
    s.id is (primaryKey, dbType("raw(16)"))))

  on(partnermappings)(s => declare(
    s.partner is (dbType("raw(16)"))))

  def test = if (false) {
    import org.restlet.data.Method
    import org.squeryl.PrimitiveTypeMode._
    import org.squeryl.PrimitiveTypeMode.createOutMapperStringType
    import org.squeryl.PrimitiveTypeMode.string2ScalarString
    import org.squeryl.PrimitiveTypeMode.transaction
    import org.squeryl.PrimitiveTypeMode.traversableOfString2ListString
    import org.squeryl.PrimitiveTypeMode.where

    transaction {

      val schema = de.man.mn.gep.scala.config.enovia5.metadata.inmemory.partner.PartnerSchema
      schema.printDdl(println(_))

    }
  }

}

