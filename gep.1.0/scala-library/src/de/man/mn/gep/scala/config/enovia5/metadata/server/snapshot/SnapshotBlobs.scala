package de.man.mn.gep.scala.config.enovia5.metadata.server.snapshot

import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.util.zip.GZIPInputStream

import org.squeryl.PrimitiveTypeMode.createOutMapperStringType
import org.squeryl.PrimitiveTypeMode.string2ScalarString
import org.squeryl.PrimitiveTypeMode.transaction
import org.squeryl.PrimitiveTypeMode.traversableOfString2ListString
import org.squeryl.PrimitiveTypeMode.where

import com.ibm.de.ebs.plm.scala.json.JsonConversions.Any2Json
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2Array
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2Object
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2String
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.database.SquerylHelpers.hextoraw
import com.ibm.de.ebs.plm.scala.util.Io.copyText

import SnapshotSchema.snapshots
import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

class SnapshotBlobs extends DatabaseRepresentation {

  lazy val id = parameters("snapshot")

  override def doWrite(writer: PrintWriter) = {
    try {
      transaction {
        val query = org.squeryl.PrimitiveTypeMode.from(snapshots) { s =>
          where(s.id === hextoraw(id) and (s.project in projectlist)) select (
            if (baseuri.contains("/formats/details/")) s.formatsdetails
            else if (baseuri.contains("/formats/summary/")) s.formatssummary
            else if (baseuri.contains("/bom/")) s.bom
            else if (baseuri.contains("/whereused/")) s.whereused
            else if (baseuri.contains("/graph/spacetree/")) s.spacetree
            else if (baseuri.contains("/graph/millertree/")) s.millertree
            else if (baseuri.contains("/assembly/")) s.assembly
            else null)
        }
        for (blob <- query) {
          val in = new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(blob)), "UTF-8")
          copyText(in, writer)
        }
      }
    } catch {
      case e => throw e
    }
  }

}