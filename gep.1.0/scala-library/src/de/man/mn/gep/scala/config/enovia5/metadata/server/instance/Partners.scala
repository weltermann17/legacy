package de.man.mn.gep.scala.config.enovia5.metadata.server.instance

import java.io.PrintWriter

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.ps2Rich
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper

import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

class Partners extends DatabaseRepresentation {

  parent =>

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

  override def doWrite(writer: PrintWriter) = {
    def parentlink(parentid: Option[String]): Map[String, String] = {
      if (parentid.isDefined) Map("parent" -> (baseuri.substring(0, baseuri.indexOf("versions/")) + "versions/" + parentid.get + "/")) else Map()
    }
    try {
      writer.print("{\"response\":{\"data\":[")
      var rows = from
      dummyData.foreach { p =>
        if (from < rows) writer.print(",")
        writer.print(p)
        rows += 1
      }
      writer.print("],\"startRow\":")
      writer.print(from)
      writer.print(",\"endRow\":")
      writer.print(rows)
      writer.print(",\"totalRows\":")
      writer.print(computeTotal(rows))
      writer.print(",\"status\":0}}")
    } catch {
      case e =>
        writer.print("],\"status\":-1}}")
        throw e
    }
  }

}
