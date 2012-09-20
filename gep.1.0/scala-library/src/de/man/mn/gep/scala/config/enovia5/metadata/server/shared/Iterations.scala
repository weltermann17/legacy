package de.man.mn.gep.scala.config.enovia5.metadata.server.shared

import java.io.PrintWriter
import java.io.StringWriter

import org.restlet.data.MediaType

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.iso8601
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.nstring2s
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.ps2Rich
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Long
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Timestamp
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper
import com.ibm.de.ebs.plm.scala.rest.Services
import com.ibm.de.ebs.plm.scala.text.StringConversions.toHexString
import com.ibm.de.ebs.plm.scala.util.Converters.bytesToKb
import com.ibm.de.ebs.plm.scala.util.Converters.bytesToMb

import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

class Iterations extends DatabaseRepresentation with HasFormats {

  override lazy val sql = (subtype match {

    case Versions(_) | Instances(_) => """select /*+ result_cache */ /*+ all_rows */
it.oid as oid
from 
enovia.mandocumentiteration it, 
enovia.mandocumentrevision dr
where it.oid = dr.vpreferrediteration 
and dr.oid in (
select dr.oid from 
enovia.mandocumentrevision dr, 
enovia.vpmtprelationcfroma vtrf, 
enovia.vpmtprelationctoa vtrt 
where utl_raw.substr(vtrt.cto, 2, 16) = dr.oid 
and vtrf.oid = vtrt.oid 
and vtrf.cfrom in (
select hextoraw('10'||vpv||cfromsuffix) 
from 
enovia.mantprelationcfromahelper, (
select vpv from (#INNERCONNECTBY#) )))
"""

    case Products(_) => """ select /*+ result_cache */ /*+ all_rows */
it.oid as oid
from 
enovia.mandocumentiteration it, 
enovia.mandocumentrevision dr
where it.oid = dr.vpreferrediteration 
and dr.oid in (
select utl_raw.substr(vtrt.cto, 2, 16) from 
enovia.vpmtprelationcfroma vtrf,
enovia.vpmtprelationctoa vtrt
where vtrt.oid = vtrf.oid
and vtrf.cfrom in (
select hextoraw('10'||vpv||cfromsuffix) 
from
enovia.mantprelationcfromahelper, 
(#INNERCONNECTBY#) ) )  
"""
  })
    .replace("#INNERCONNECTBY#", subtype.connectbyprior)

  case class IterationsRow(
    oid: String)
    extends PropertiesMapper

  override def doWrite(writer: PrintWriter) = {
    try {
      writer.print("{\"response\":{\"data\":[")
      var rows = 0
      for (
        row <- statement <<? subtype.repeats << subtype.id <<! (result => IterationsRow(result))
      ) {
        if (from < rows) writer.print(",")
        writer.print("\"")
        writer.print(row.oid)
        writer.print("\"")
        rows += 1
      }
      writer.print("],\"status\":0}}")
    } catch {
      case e =>
        writer.print("],\"status\":-1}}")
        throw e
    }
  }

}
