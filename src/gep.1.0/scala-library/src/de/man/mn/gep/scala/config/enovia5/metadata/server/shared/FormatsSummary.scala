package de.man.mn.gep.scala.config.enovia5.metadata.server.shared

import java.io.PrintWriter

import org.restlet.data.MediaType

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.ps2Rich
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Long
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper
import com.ibm.de.ebs.plm.scala.rest.Services
import com.ibm.de.ebs.plm.scala.util.Converters.bytesToKb
import com.ibm.de.ebs.plm.scala.util.Converters.bytesToMb

import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

class FormatsSummary extends DatabaseRepresentation with HasFormats {

  override lazy val sql = (subtype match {

    case Versions(_) | Instances(_) => """select /*+ result_cache */ /*+ all_rows */
if.vformattype as formattype, 
lower(substr(utl_raw.cast_to_varchar2(sf.vvaultdoc), 9, 3)), 
sum(sf.v514filesize) as b
from 
enovia.mandocsecuredfile sf, 
enovia.mandociterationformat if, 
enovia.mandocumentiteration it, 
enovia.mandocumentrevision dr
where sf.viterationformat = if.oid 
and if.viteration = it.oid 
and it.oid = dr.vpreferrediteration 
and dr.oid in (
select dr.oid from 
enovia.mandocumentrevision dr, 
enovia.vpmtprelationcfroma vtrf, 
enovia.vpmtprelationctoa vtrt 
where utl_raw.substr(vtrt.cto, 2, 16) = dr.oid 
and vtrf.oid = vtrt.oid 
and vtrf.rank = vtrt.rank 
and vtrf.cfrom in (
select hextoraw('10'||vpv||cfromsuffix) 
from 
enovia.mantprelationcfromahelper, (
select vpv from (#INNERCONNECTBY#) )))
group by if.vformattype, lower(substr(utl_raw.cast_to_varchar2(sf.vvaultdoc), 9, 3))
order by formattype, b desc
"""

    case Products(_) => """ select /*+ result_cache */ /*+ all_rows */
if.vformattype as formattype, 
lower(substr(utl_raw.cast_to_varchar2(sf.vvaultdoc), 9, 3)), 
sum(sf.v514filesize) as b
from 
enovia.mandocsecuredfile sf, 
enovia.mandociterationformat if, 
enovia.mandocumentiteration it, 
enovia.mandocumentrevision dr
where sf.viterationformat = if.oid 
and if.viteration = it.oid 
and it.oid = dr.vpreferrediteration 
and dr.oid in (
select utl_raw.substr(vtrt.cto, 2, 16) from 
enovia.vpmtprelationcfroma vtrf,
enovia.vpmtprelationctoa vtrt
where vtrt.oid = vtrf.oid
and vtrf.rank = vtrt.rank 
and vtrf.cfrom in (
select hextoraw('10'||vpv||cfromsuffix) 
from
enovia.mantprelationcfromahelper, 
(#INNERCONNECTBY#) ) )  
group by if.vformattype, lower(substr(utl_raw.cast_to_varchar2(sf.vvaultdoc), 9, 3))
order by formattype, b desc
"""
  })
    .replace("#INNERCONNECTBY#", subtype.connectbyprior)

  def this(parameters: Map[String, String])(implicit baseuri: String, connectionfactory: ConnectionFactory) = {
    this()
    init(baseuri, connectionfactory, parameters)
  }

  private case class PerVault(
    mimetype: String,
    vault: String,
    filesize: Long)
    extends PropertiesMapper {

    val filesize_kb = bytesToKb(filesize, 0).toLong
    val filesize_mb = bytesToMb(filesize)

    val extension = Services.Metadata.getExtension(MediaType.valueOf(mimetype))

  }

  override def doWrite(writer: PrintWriter) = {
    try {
      var rows = from
      writer.print("{\"response\":{\"data\":[")
      for (
        pervault <- statement <<? subtype.repeats << subtype.id <<! (result =>
          PerVault(
            result,
            result,
            result))
      ) {
        if (0 < rows) writer.print(",")
        writer.print(pervault)
        rows += 1
      }
      writer.print("],\"startRow\":")
      writer.print(from)
      writer.print(",\"endRow\":")
      writer.print(rows)
      writer.print(",\"totalRows\":")
      writer.print(rows)
      writer.print(",\"status\":0}}")
    } catch {
      case e =>
        writer.print("],\"status\":-1}}")
        throw e
    }
  }
}
