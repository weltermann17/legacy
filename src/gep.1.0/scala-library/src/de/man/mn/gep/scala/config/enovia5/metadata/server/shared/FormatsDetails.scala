package de.man.mn.gep.scala.config.enovia5.metadata.server.shared

import java.io.PrintWriter

import org.restlet.data.MediaType

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.nstring2s
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.ps2Rich
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.rest.Services

import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

class FormatsDetails extends DatabaseRepresentation with HasFormats {

  override lazy val sql = (subtype match {

    case Versions(_) | Instances(_) => """select /*+ result_cache */ /*+ all_rows */
if.vformattype as formattype, 
lower(substr(utl_raw.cast_to_varchar2(sf.vvaultdoc), 9, 3)) as vault,
utl_raw.cast_to_raw(dm.vid) as filename,
substr(doclocation, instr(doclocation, '/secured')) as doclocation
from 
enovia.mandocsecuredfile sf, 
enovia.mandociterationformat if, 
enovia.mandocumentiteration it,
enovia.mantpdocumentmaster dm, 
enovia.mandocumentrevision dr, 
(#VAULTUNIONALL#)
where 
void = hextoraw(substr(utl_raw.cast_to_varchar2(sf.vvaultdoc), 15))
and sf.viterationformat = if.oid 
and if.viteration = it.oid 
and it.oid = dr.vpreferrediteration 
and dm.oid = dr.vmaster
and exists ( 
select * from enovia.vpmtpproperty vtpp
where vtpp.cowner = dr.vhistoid
and vtpp.type = hextoraw('80A2B3BD0000520C383BE4950005A450') )
and dr.oid in (
select dr.oid from 
enovia.mandocumentrevision dr, 
enovia.vpmtprelationcfroma vtrf, 
enovia.vpmtprelationctoa vtrt 
where utl_raw.substr(vtrt.cto, 2, 16) = dr.oid 
and vtrf.oid = vtrt.oid 
and vtrf.cfrom in (
select hextoraw('10'||rawtohex(vpv)||cfromsuffix) 
from 
enovia.mantprelationcfromahelper, (
select vpv from (#INNERCONNECTBY#) )))
order by formattype asc, vault asc, sf.v514filesize desc
"""

    case Products(_) => """select /*+ result_cache */ /*+ all_rows */
if.vformattype as formattype, 
lower(substr(utl_raw.cast_to_varchar2(sf.vvaultdoc), 9, 3)) as vault,
utl_raw.cast_to_raw(dm.vid) as filename,
substr(doclocation, instr(doclocation, '/secured')) as doclocation
from 
enovia.mandocsecuredfile sf, 
enovia.mandociterationformat if, 
enovia.mandocumentiteration it,
enovia.mantpdocumentmaster dm, 
enovia.mandocumentrevision dr, 
(#VAULTUNIONALL#)
where 
void = hextoraw(substr(utl_raw.cast_to_varchar2(sf.vvaultdoc), 15))
and sf.viterationformat = if.oid 
and if.viteration = it.oid 
and it.oid = dr.vpreferrediteration 
and dm.oid = dr.vmaster
and exists ( 
select * from enovia.vpmtpproperty vtpp
where vtpp.cowner = dr.vhistoid
and vtpp.type = hextoraw('80A2B3BD0000520C383BE4950005A450') )
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
(#INNERCONNECTBY#) ))
order by formattype asc, vault asc, sf.v514filesize desc
"""
  })
    .replace("#VAULTUNIONALL#", if (baseuri.contains("truck")) truckvaults else if (baseuri.contains("engine")) enginevaults else "#ERROR#")
    .replace("#INNERCONNECTBY#", subtype.connectbyprior)

  def this(parameters: Map[String, String])(implicit baseuri: String, connectionfactory: ConnectionFactory) = {
    this()
    init(baseuri, connectionfactory, parameters)
  }

  private case class FormatDetail(
    mimetype: String,
    vault: String,
    filename: NString,
    filepath: String)
    extends PropertiesMapper {

    private val extension = Services.Metadata.getExtension(MediaType.valueOf(mimetype))

    override def toMap = Map(
      "filename" -> (filename + "." + extension),
      "filepath" -> filepath)

  }

  private def validate(filename: NString) = {
    filename.toString.replace("*", "0").replace("!", "1").replace("?", "2")
  }

  override def doWrite(writer: PrintWriter) = {
    try {
      var currentmimetype = ""
      var currentvault = ""
      var rows = from
      writer.print("{\"response\":{\"data\":{")
      for (
        formatdetail <- statement <<? subtype.repeats << subtype.id <<! (result =>
          FormatDetail(
            validate(result),
            result,
            result,
            result))
      ) {
        if (currentmimetype != formatdetail.mimetype) {
          currentmimetype = formatdetail.mimetype
          if (0 < rows) writer.print("]},")
          writer.print(Json.build(currentmimetype))
          writer.print(":{")
          currentvault = ""
          rows = 0
        }
        if (currentvault != formatdetail.vault) {
          currentvault = formatdetail.vault
          if (0 < rows) writer.print("],")
          writer.print(Json.build(currentvault))
          writer.print(":[")
          rows = 0
        }
        if (0 < rows) writer.print(",")
        writer.print(formatdetail)
        rows += 1
      }
      writer.print("]}},\"status\":0}}")
    } catch {
      case e =>
        writer.print("],\"status\":-1}}")
        throw e
    }
  }

  private val truckvaults = """select oid as void, doclocation 
from 
muclv.vaultdocument 
union all
select oid as void, doclocation 
from 
poslv.vaultdocument 
union all
select oid as void, doclocation 
from 
slzlv.vaultdocument 
union all
select oid as void, doclocation 
from 
stwlv.vaultdocument 
union all
select oid as void, doclocation 
from 
vielv.vaultdocument 
union all
select oid as void, doclocation 
from 
stylv.vaultdocument"""

  private val enginevaults = """select oid as void, doclocation 
from 
nbglv.vaultdocument 
union all
select oid as void, doclocation 
from 
stylv.vaultdocument
"""

}
