package de.man.mn.gep.scala.config.enovia5.metadata.server.snapshot

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
import SnapshotSchema.snapshots
import SnapshotSchema.snapshot2iteration

class Snapshot extends DatabaseRepresentation {

  lazy val id = parameters("snapshot")

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
      case e =>
        writer.print("],\"status\":-1}}")
        throw e
    }
  }

  private def doGet(writer: PrintWriter)(implicit r: Int) = {
    var rows = r
    val query = org.squeryl.PrimitiveTypeMode.from(snapshots)(s =>
      where(s.id === hextoraw(id) and (s.project in projectlist)) select (new SnapshotDetail(s)))
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

    org.squeryl.PrimitiveTypeMode.update(snapshots)(s =>
      where(s.id === hextoraw(id) and (s.project in projectlist)) set {

        List(
          if (2 == newvalues.size && newvalues.contains("lockowner")) {
            List(
              {
                val lockowner = newvalues("lockowner").asString
                List(
                  s.lockstatus := (if (0 < lockowner.length) Some("Y") else None),
                  s.lockuser := (if (0 < lockowner.length) Some(lockowner) else None))
              }).flatten
          } else {

            List(
              if (newvalues.contains("lockowner")) {
                val lockowner = newvalues("lockowner").asString
                List(
                  s.lockstatus := (if (0 < lockowner.length) Some("Y") else None),
                  s.lockuser := (if (0 < lockowner.length) Some(lockowner) else None))
              } else {
                nullassign
              },
              if (newvalues.contains("description_de")) {
                val description = newvalues("description_de").asString
                List(s.description_de := (if (null != description && 0 < description.length) Some(description) else None))
              } else {
                nullassign
              },
              if (newvalues.contains("name")) {
                List(s.name := newvalues("name").asString)
              } else {
                nullassign
              },
              if (newvalues.contains("expirationdate")) {
                val format = new java.text.SimpleDateFormat("yyyy-MM-dd")
                val expirationdate = format.parse(newvalues("expirationdate").asString).getTime
                List(s.expirationdate := new Timestamp(expirationdate))
              } else {
                nullassign
              },
              List(s.lastmodified := new Timestamp(System.currentTimeMillis))).flatten
          }).flatten
          .filter(
            nullassignment != _): _*

      })

    doGet(writer)
  }

  private def doDelete(writer: PrintWriter)(implicit r: Int) = {
    case class DeleteInfo(
      id: String,
      snapshotsdeleted: Int,
      iterationsdeleted: Int) extends PropertiesMapper
    var rows = r
    val deleteinfo = DeleteInfo(
      id,
      snapshots.deleteWhere(s => (s.id === hextoraw(id) and (s.project in projectlist))),
      snapshot2iteration.deleteWhere(s2i => s2i.snapshot === hextoraw(id)))
    writer.print(deleteinfo)
    rows += 1
    rows
  }

}