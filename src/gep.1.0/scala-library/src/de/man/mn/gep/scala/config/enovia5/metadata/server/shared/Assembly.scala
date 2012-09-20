package de.man.mn.gep.scala.config.enovia5.metadata.server.shared

import java.io.PrintWriter

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.nstring2s
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.ps2Rich
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Double
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.s2nstring
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper

import de.man.mn.gep.scala.config.enovia5.metadata.server.Conversions.status
import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

abstract class Assembly extends DatabaseRepresentation with HasFormats {

  def this(parameters: Map[String, String])(implicit baseuri: String, connectionfactory: ConnectionFactory) = {
    this()
    init(baseuri, connectionfactory, parameters)
  }

  private[Assembly] object Versions {

    def get(id: String)(implicit instance: Instance = null) = versions.get(id) match {

      case Some(version) => version
      case None =>
        count += 1
        val version = Version(
          count,
          instance.partnumber,
          2 == instance.parttype,
          instance.version,
          instance.status,
          instance.owner.toString,
          instance.description.getOrElse(""))
        versions.put(id, version)
        version
    }

    def contains(id: String) = versions.contains(id)

    def remove(id: String) = versions.remove(id) match {
      case Some(version) => version
      case None => null
    }

    def ids = versions.keySet.toList

    private var count = 0
    private val versions = new collection.mutable.HashMap[String, Version]
  }

  private def partnervalue(name: String, attribute: String) = {
    dummyData.find { p => p.manpartnumber.toString.equalsIgnoreCase(name.toString) } match {
      case Some(p) =>
        attribute match {
          case "system" => Some(p.system)
          case "partnumber" => Some(p.partnumber)
          case "versionstring" => Some(p.versionstring)
          case _ => None
        }
      case None => None
    }
  }

  private[Assembly] case class Version(
    id: Int,
    partnumber$: NString,
    isassembly: Boolean,
    version$: NString,
    status: String,
    owner$: String,
    description: NString)
    extends PropertiesMapper {

    val partnumber = partnervalue(partnumber$, "partnumber") match {
      case Some(v) => v
      case None => partnumber$
    }

    val version = partnervalue(partnumber$, "versionstring") match {
      case Some(v) => v
      case None => version$
    }

    val owner = partnervalue(partnumber$, "owner") match {
      case Some(v) => v
      case None => owner$
    }

  }

  private val assemblyrelations = new collection.mutable.HashSet[String]

  private[Assembly] case class Instance(
    level: Int,
    m1: Double, m2: Double, m3: Double, m4: Double, m5: Double, m6: Double, m7: Double, m8: Double, m9: Double, m10: Double, m11: Double, m12: Double,
    aggregatedby: String,
    assemblyrelation: String,
    versionid: String,
    partnumber$: NString,
    parttype: Int,
    version$: NString,
    status: String,
    owner$: String,
    description: Option[NString])
    extends PropertiesMapper {

    val partnumber = partnervalue(partnumber$, "partnumber") match {
      case Some(v) => v
      case None => partnumber$
    }

    val version = partnervalue(partnumber$, "versionstring") match {
      case Some(v) => v
      case None => version$
    }

    val owner = partnervalue(partnumber$, "owner") match {
      case Some(v) => v
      case None => owner$
    }

    override def equals(other: Any): Boolean = assemblyrelation == other.asInstanceOf[Instance].assemblyrelation

    override def hashCode = assemblyrelation.hashCode

    private val isroot = 0 == aggregatedby.length

    private implicit val _ = this

    val children = new collection.mutable.HashSet[Instance]

    def write(writer: PrintWriter, aggregatedby: Int, comma: Int): Unit = {
      if (0 < comma) writer.print(",")
      writer.print("{\"m1\":" + m1)
      writer.print(",\"m2\":" + m2)
      writer.print(",\"m3\":" + m3)
      writer.print(",\"m4\":" + m4)
      writer.print(",\"m5\":" + m5)
      writer.print(",\"m6\":" + m6)
      writer.print(",\"m7\":" + m7)
      writer.print(",\"m8\":" + m8)
      writer.print(",\"m9\":" + m9)
      writer.print(",\"m10\":" + m10)
      writer.print(",\"m11\":" + m11)
      writer.print(",\"m12\":" + m12)
      writer.print(",\"aggregatedby\":" + aggregatedby)
      writer.print(",\"instanceof\":" + Versions.get(versionid).id)
      writer.print("}")
      children.toList.sortWith((a, b) => a.partnumber + a.version < b.partnumber + b.version).foreach(
        _.write(writer, Versions.get(versionid).id, comma + 1))
      children.clear
    }

  }

  case class Partner(
    row: Int,
    name: NString,
    system: NString,
    partnumber: NString,
    versionstring: NString,
    mansystem: NString,
    manpartnumber: String,
    manversionstring: String,
    description: Option[NString],
    lastmodified: java.sql.Timestamp,
    owner: NString)
    extends PropertiesMapper {

    val datatype = "partners"

    val links = {
      List(
        ("owner", "/users/details/" + owner + "/")).toMap
    }

  }

  private lazy val dummyData = {
    val now = new java.sql.Timestamp(com.ibm.de.ebs.plm.scala.util.Timers.now)
    List(
      Partner(1, "VW Nutzfahrzeuge", "KVS", ".23B.213.183.A", "1", "Enovia5", "85.12340-0102", "_B_", Some("HALTER  F KRAFTSTOFFLEITUNGEN"), now, "C5998"),
      Partner(2, "VW Nutzfahrzeuge", "KVS", ".23B.213.201.", "1", "Enovia5", "85.12201-6101", "_O_", Some("ZSB KRAFTSTOFFBEHAELTER  100L"), now, "C5998"),
      Partner(3, "VW Nutzfahrzeuge", "KVS", ".23B.213.202.B", "1", "Enovia5", "81.12201-5568", "01_", Some("KRAFTSTOFFBEHAELTER  100L"), now, "C5998"),
      Partner(4, "VW Nutzfahrzeuge", "KVS", ".23B.213.203.", "1", "Enovia5", "81.12210-6025", "_A_", Some("ZSB TANKVERSCHLUSS  NICHT ABSPERRBAR UNBELUEFTET"), now, "C5998"),
      Partner(5, "VW Nutzfahrzeuge", "KVS", ".23B.213.204.D", "1", "Enovia5", "81.12210-6029", "_B_", Some("ZSB TANKVERSCHLUSS  F GLEICHSCHIESSUNG BELUEFTET"), now, "C5998"),
      Partner(6, "VW Nutzfahrzeuge", "KVS", ".23B.213.205.", "1", "Enovia5", "81.27203-6016", "06_", Some("ZSB KOMBIGEBER  TX D 385"), now, "C5998"),
      Partner(7, "VW Nutzfahrzeuge", "KVS", ".23B.213.206.", "1", "Enovia5", "81.98181-0227", "01_", Some("VERSCHLUSSTOPFEN  DMR 4MM"), now, "C5998"))
  }

  protected val repeats: Int

  override def doWrite(writer: PrintWriter) = {
    try {
      writer.print("{\"response\":{\"instances\":[")
      val stack = new collection.mutable.Stack[Instance]
      for (
        instance <- statement <<? repeats << subtype.id <<! (result =>
          Instance(
            result,
            result,
            result,
            result,
            result,
            result,
            result,
            result,
            result,
            result,
            result,
            result,
            result,
            result,
            result,
            result,
            result,
            result,
            result,
            status(result),
            result,
            result))
      ) {
        if (!assemblyrelations.contains(instance.assemblyrelation)) {
          assemblyrelations += instance.assemblyrelation
          if (0 < stack.size) {
            while (math.abs(instance.level) <= math.abs(stack.top.level)) stack.pop
            stack.top.children += instance
          }
          stack.push(instance)
        }
      }
      val root = stack.last
      stack.clear
      root.write(writer, 0, 0)
      var i = 0
      writer.print("],\"versions\":[")
      Versions.ids.foreach { id =>
        if (0 < i) writer.print(",")
        writer.print(Versions.remove(id))
        i += 1
      }
      writer.print("],\"status\":0}}")
    } catch {
      case e =>
        writer.print("{}")
        throw e
    }
  }

}
