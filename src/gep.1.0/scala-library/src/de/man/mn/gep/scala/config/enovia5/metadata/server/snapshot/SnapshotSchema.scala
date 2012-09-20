package de.man.mn.gep.scala.config.enovia5.metadata.server.snapshot

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

class SnapshotTable(
  @Column(name = "OID", length = 32) val id: String = Uuid.newType4Uuid.toUpperCase,
  @Column(name = "CCREATED") val created: Timestamp = new Timestamp(System.currentTimeMillis),
  @Column(name = "CMODIFIED") var lastmodified: Timestamp = new Timestamp(System.currentTimeMillis),
  @Column(name = "LOCKSTATUS", length = 1) var lockstatus: Option[String] = None,
  @Column(name = "LOCKUSER", length = 32) var lockuser: Option[String] = None,
  @Column(name = "VUSER", length = 32) var owner: String,
  @Column(name = "VORGANIZATION", length = 32) var team: String,
  @Column(name = "VPROJECT", length = 32) var project: String,
  @Column(name = "VPARENT", length = 32) val parentoid: String,
  @Column(name = "VPARENTTYPE", length = 40) val parenttype: String,
  @Column(name = "VPARENTNAME", length = 80) val parentname: String,
  @Column(name = "CEXPIRATIONDATE") var expirationdate: Timestamp,
  @Column(name = "VBYTESSTORED") val bytesstored: Int,
  @Column(name = "VITERATIONSTAGGED") val iterationstagged: Int,
  @Column(name = "VASSEMBLY") val assembly: Array[Byte],
  @Column(name = "VBOM") val bom: Array[Byte],
  @Column(name = "VWHEREUSED") val whereused: Array[Byte],
  @Column(name = "VSPACETREE") val spacetree: Array[Byte],
  @Column(name = "VMILLERTREE") val millertree: Array[Byte],
  @Column(name = "VFORMATSSUMMARY") val formatssummary: Array[Byte],
  @Column(name = "VFORMATSDETAILS") val formatsdetails: Array[Byte],
  @Column(name = "VNAME", length = 80) var name: String,
  @Column(name = "VDESCRIPTIONDE", length = 250) var description_de: Option[String] = None)

  extends KeyedEntity[String] {

  def update = {
    lastmodified = new Timestamp(System.currentTimeMillis)
    SnapshotSchema.snapshots.update(this)
  }

}

class SnapshotDetail(
  val id: String,
  val row: Option[Int],
  val name: String,
  val description_de: Option[String],
  val created: Option[Timestamp],
  val lastmodified: Timestamp,
  val expirationdate: Timestamp,
  val owner: String,
  val team: Option[String],
  val project: Option[String],
  val parentname: String,
  val parentoid: String,
  val parenttype: String,
  val iterationstagged: String,
  lockstatus: Option[String],
  lockuser: Option[String],
  bytesstored: Int)(implicit databaserepresentation: DatabaseRepresentation, parameters: Map[String, String])

  extends PropertiesMapper {

  def this(s: SnapshotTable)(implicit databaserepresentation: DatabaseRepresentation, parameters: Map[String, String]) = {
    this(
      s.id,
      None,
      s.name,
      s.description_de,
      Some(s.created),
      s.lastmodified,
      s.expirationdate,
      s.owner,
      Some(s.team),
      Some(s.project),
      s.parentname,
      s.parentoid,
      s.parenttype,
      s.iterationstagged.toString,
      s.lockstatus,
      s.lockuser,
      s.bytesstored)(databaserepresentation, parameters)
  }

  val datatype = "snapshots"
  val isassembly = true
  lazy val storage = Converters.bytesToMb(bytesstored, 2) + " mb"
  lazy val lockowner = Conversions.lockowner(lockstatus, lockuser)
  lazy val editable = lockowner match { case Some(lo) => lo == parameters("authorization-identifier") case None => false }
  lazy val links = {
    val base = databaserepresentation.baseuri
    val url = base.substring(0, base.indexOf("snapshots/")) + "snapshots/" + id
    List(
      ("snapshot", url + "/"),
      (parenttype, base.substring(0, base.indexOf("snapshots/")) + parenttype + "/?equal&id&0x" + parentoid + "&sort&id"),
      ("formatssummary", url + "/formats/summary/"),
      ("formatsdetails", url + "/formats/details/"),
      ("assembly", url + "/assembly/E137A1C70221436FB881EE2773787EE2/"),
      ("bom", url + "/bom/?equal&id&*"),
      ("whereused", url + "/whereused/?equal&id&*"),
      ("spacetree", url + "/graph/spacetree/0/"),
      ("millertree", url + "/graph/millertree/0/"),
      ("owner", "/users/details/" + owner + "/"),
      ("lockowner", "/users/details/" + lockowner.getOrElse("") + "/")).toMap
  }

}

class Snapshot2Iteration(
  @Column(name = "VSNAPSHOT", length = 32) val snapshot: String = null,
  @Column(name = "VITERATION", length = 32) val iteration: String = null)

object SnapshotSchema extends Schema {

  val snapshots = table[SnapshotTable]("ENOREAD.MANSNAPSHOT")
  val snapshot2iteration = table[Snapshot2Iteration]("ENOREAD.MANSNAPSHOT2ITERATION")

  on(snapshots)(s => declare(
    s.description_de is (indexed("ENOREAD.MANSNAPSHOTI01")),
    s.name is (indexed("ENOREAD.MANSNAPSHOTI02")),
    s.lastmodified is (indexed("ENOREAD.MANSNAPSHOTI03")),
    s.owner is (indexed("ENOREAD.MANSNAPSHOTI04")),
    s.project is (indexed("ENOREAD.MANSNAPSHOTI05")),
    s.parentoid is (indexed("ENOREAD.MANSNAPSHOTI06"), dbType("raw(16)")),
    s.parentname is (indexed("ENOREAD.MANSNAPSHOTI07")),
    s.expirationdate is (indexed("ENOREAD.MANSNAPSHOTI08")),
    s.id is (primaryKey, dbType("raw(16)"))))

  on(snapshot2iteration)(s => declare(
    s.snapshot is (dbType("raw(16)")),
    s.iteration is (dbType("raw(16)")),
    s.iteration is (indexed("ENOREAD.MANSNAPSHOT2ITERATIONI01")),
    columns(s.snapshot, s.iteration) are (indexed("ENOREAD.MANSNAPSHOT2ITERATIONI02"), unique)))

}

