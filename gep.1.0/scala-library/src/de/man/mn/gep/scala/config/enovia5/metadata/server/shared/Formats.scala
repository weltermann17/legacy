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

class Formats extends DatabaseRepresentation with HasFormats {

  override lazy val sql = """select /*+ result_cache */ /*+ all_rows */
utl_raw.cast_to_raw(dm.vid||dr.vversion),
if.vformattype,
utl_raw.cast_to_varchar2(if.v505submimetype),
sf.v514filesize,
sf.v514lastmodified,
substr(utl_raw.cast_to_varchar2(sf.vvaultdoc), 9, 5), 
substr(utl_raw.cast_to_varchar2(sf.vvaultdoc), 15)
from 
enovia.mandocsecuredfile sf, 
enovia.mandociterationformat if, 
enovia.mandocumentiteration it, 
enovia.mandocumentrevision dr,
enovia.mantpdocumentmaster dm
where sf.viterationformat = if.oid 
and if.viteration = it.oid 
and it.oid = dr.vpreferrediteration 
and dm.oid = dr.vmaster
and dr.oid in (
select dr.oid from 
enovia.mandocumentrevision dr, 
enovia.vpmtprelationcfroma vtrf, 
enovia.vpmtprelationctoa vtrt 
where utl_raw.substr(vtrt.cto, 2, 16) = dr.oid 
and vtrf.oid = vtrt.oid 
and vtrf.cfrom in (
select hextoraw('10'||:1||cfromsuffix) 
from 
enovia.mantprelationcfromahelper))
order by sf.v514filesize asc
"""

  case class NativeFormat(
    name: NString,
    mimetype: String,
    submimetype: String,
    filesize: Long,
    lastmodified: String,
    vault: String,
    documentid: String)
    extends PropertiesMapper {

    val filesize_kb = bytesToKb(filesize, 0).toLong
    val filesize_mb = bytesToMb(filesize)
    lazy val location = vault.substring(0, 3).toLowerCase
    lazy val city = location match {
      case "muc" => "M\u00fcnchen"
      case "nbg" => "N\u00fcrnberg"
      case "sty" => "Steyr"
      case "slz" => "Salzgitter"
      case "vie" => "Wien"
      case "pos" => "Posen"
      case "stw" => "Starachowice"
      case _ => "Unknown"
    }
    lazy val extension = Services.Metadata.getExtension(MediaType.valueOf(mimetype))
    lazy val nativeformat = extension.toLowerCase
    lazy val filepath = {
      val vaultdocument = new VaultDocument(parameters ++ Map("vault" -> vault, "oid" -> documentid))(baseuri, connectionfactory)
      val writer = new StringWriter
      vaultdocument.write(new PrintWriter(writer))
      val doclocation = writer.toString
      doclocation.substring(doclocation.indexOf("/secured"))
    }
    lazy val url = baseuri.substring(0, baseuri.indexOf(subtype.base)) + "vaults/" + location + "/nativeformats/" + nativeformat + "/" + documentid + "/" + toHexString(filepath) + "/" + toHexString(name + "." + extension) + "/"
    lazy val page = try { name.substring(18, 22) } catch { case e => "0001" }
  }

  case class DerivedFormat(
    extension: String)
    extends PropertiesMapper {

    val name = ""
    val filesize = -1

    lazy val derivedformat = extension match {
      case "CATProduct.jar" => "catproduct"
      case "3dxml" => "3dxml"
      case "vfz" => "plmxml"
      case f => throw new Exception("Unknown derived format: " + f)
    }

    lazy val url = {
      val url = baseuri.replace(subtype.template, subtype.id)
      url.substring(0, url.indexOf("formats/")) + "derivedformats/" + derivedformat + "/" + toHexString("TEST." + extension) + "/E137A1C70221436FB881EE2773787EE2/0/"
    }
  }

  override def doWrite(writer: PrintWriter) = {
    try {
      writer.print("{\"response\":{\"data\":[")
      var rows = from
      var total = 0.
      var native3d = false
      for (
        format <- statement << subtype.id <<! (result =>
          NativeFormat(
            result,
            result,
            result,
            result,
            iso8601(result),
            result,
            result))
      ) {
        if (0 < rows) writer.print(",")
        writer.print(format)
        rows += 1
        if (List("CATPart", "cgr").contains(format.extension)) native3d = true
      }
      if (!native3d) {
        def derived(extension: String) = {
          if (0 < rows) writer.print(",")
          writer.print(DerivedFormat(extension))
          rows += 1
        }
        derived("CATProduct.jar")
        derived("3dxml")
        derived("vfz")
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
