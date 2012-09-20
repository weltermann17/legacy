package de.man.mn.gep.scala.config.enovia5.metadata.server.partnerversion

import java.io.PrintWriter
import java.sql.Timestamp

import org.restlet.data.Method
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.PrimitiveTypeMode.createOutMapperStringType
import org.squeryl.PrimitiveTypeMode.string2ScalarString
import org.squeryl.PrimitiveTypeMode.transaction
import org.squeryl.PrimitiveTypeMode.traversableOfString2ListString
import org.squeryl.PrimitiveTypeMode.where
import org.squeryl.dsl.ast.UpdateAssignment

import com.ibm.de.ebs.plm.scala.database.SquerylHelpers.hextoraw
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper
import com.ibm.de.ebs.plm.scala.util.Converters.bytesToMb
import com.ibm.de.ebs.plm.scala.json.JsonConversions._
import com.ibm.de.ebs.plm.scala.json.Json

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

import de.man.mn.gep.scala.config.enovia5.metadata.server.partnerversion._
import PartnerVersionSchema.partnerversions

class PartnerVersion extends DatabaseRepresentation {

  lazy val id = parameters("partnerversion")

  override def doWrite(writer: PrintWriter) = {
    try {
      writer.print("{\"response\":{\"data\":[")
      implicit var rows = from
      if (from < rows) writer.print(",")
      transaction {
        rows += (request.getMethod match {
          case Method.GET => doGet(writer)
          case Method.PUT => doPut(writer)
          case Method.DELETE => doDelete(writer)
        })
      }
      writer.print("],\"startRow\":")
      writer.print(this.from)
      writer.print(",\"endRow\":")
      writer.print(rows)
      writer.print(",\"totalRows\":")
      writer.print(computeTotal(rows))
      writer.print(",\"status\":0}}")
    } catch {
      case e1: java.sql.SQLIntegrityConstraintViolationException =>
        writer.print("],\"errors\":{")
        writer.print("\"partnername\":{\"errorMessage\":\"Unique constraint violation\"},")
        writer.print("\"versionname\":{\"errorMessage\":\"Unique constraint violation\"}")
        writer.print("},\"status\":-4}}")
      case e: java.lang.Exception =>
        writer.print("],\"status\":-1}}")
        throw e
    }
  }

  private def doGet(writer: PrintWriter)(implicit r: Int) = {
    var rows = r
    val query = org.squeryl.PrimitiveTypeMode.from(partnerversions)(s =>
      where(s.id === hextoraw(id)) select (new PartnerVersionDetail(s)))
    for (row <- query) {
      writer.print(row)
      rows += 1
    }
    rows
  }

  private def doPut(writer: PrintWriter)(implicit r: Int) = {
    val oldvalues = jsoninput.get.get("old").asObject
    val newvalues = jsoninput.get.get("new").asObject
    val nullassignment = new UpdateAssignment(null, null)
    val nullassign = List(nullassignment)

    try {
      org.squeryl.PrimitiveTypeMode.update(partnerversions)(s =>
        where(s.id === hextoraw(id) and (s.project in projectlist)) set {
          List(

            List(
              if (newvalues.contains("description_de")) {
                val description = newvalues("description_de").asString.trim
                List(s.description_de := (if (null != description && 0 < description.length) Some(description) else None))
              } else {
                nullassign
              },
              if (newvalues.contains("name")) {
                List(s.name := newvalues("name").asString.trim)
              } else {
                nullassign
              },
              if (newvalues.contains("partner")) {
                List(s.partner := newvalues("partner").asString.trim)
              } else {
                nullassign
              },
              if (newvalues.contains("revisionstring")) {
                List(s.revisionstring := Option(newvalues("revisionstring").asString.trim))
              } else {
                nullassign
              },
              List(s.lastmodified := new Timestamp(System.currentTimeMillis))).flatten).flatten
            .filter(
              nullassignment != _): _*

        })

    } catch {
      case e1: java.lang.Exception => {
        throw e1.getCause
      }
    }

    doGet(writer)
  }

  private def doDelete(writer: PrintWriter)(implicit r: Int) = {
    case class DeleteInfo(
      id: String,
      partnerversionsdeleted: Int) extends PropertiesMapper
    var rows = r
    val deleteinfo = DeleteInfo(
      id,
      partnerversions.deleteWhere(s => (s.id === hextoraw(id))))
    writer.print(deleteinfo)
    rows += 1
    rows
  }

}