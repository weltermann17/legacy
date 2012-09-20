package de.man.mn.gep.scala.config.enovia5.metadata.server.partnerversion

import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.concurrent.Future
import java.util.zip.GZIPOutputStream
import org.restlet.data.Method
import org.squeryl.PrimitiveTypeMode.transaction
import com.ibm.de.ebs.plm.scala.concurrent.ops.future
import com.ibm.de.ebs.plm.scala.concurrent.ops.future2richfuture
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Timestamp
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Any2Json
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.util.Io.buffersize
import com.ibm.de.ebs.plm.scala.util.Timers.Int2Value
import PartnerVersionSchema.partnerversions
import de.man.mn.gep.scala.config.enovia5.metadata.server.ResultSetRepresentation
import java.sql.Date
import java.sql.Timestamp

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
import com.ibm.de.ebs.plm.scala.database.Raw

class PartnerVersions extends ResultSetRepresentation {

  override lazy val sql = """select /*+ result_cache */ /*+ first_rows(50) */ 
rnum,
rawtohex(oid),
vid,
vname,
vdescription,
vproject,
vorganization,
vuser,
lockstatus,
lockuser,
cmodified,
rawtohex(vpartner),
rawtohex(vpartmaster),
rawtohex(vpartversion),
vversion,
vrevision,
vstatus,
vreleasedate,
vcadtype,
vchangerequest,
vchangerequestpartner,
vvalidationcode,
vvalidationcodepartner,
vpda3d,
ccreated,
vmodifier,
vmodifierorganization,
vmodifierproject
from (
select t.*, rownum rnum from (
select a.*, 
p.vid as partnername, 
(m.vid||' '||v.vversion) as masterversion, 
#ORDERBYALIAS# 
from enoread.manpartnerversion a,
enoread.manpartner p,
enovia.manpartmaster m,
enovia.manpartversion v
where a.vproject in (#PROJECTS#)
and a.vpartner = p.oid
and a.vpartmaster = m.oid
and m.oid = v.vmaster
and m.v514lastversion = v.oid 
and #QUERY#
order by #ORDERBY#) 
t where rownum <= :1) 
where rnum > :2 order by orderbyalias #ASCENDINGDECENDING# 
"""
    .replace("#QUERY#", query)
    .replace("#ORDERBY#", orderby)
    .replace("#ORDERBYALIAS#", orderby.replace(" asc", " as orderbyalias").replace(" desc", " as orderbyalias"))
    .replace("#ASCENDINGDECENDING#", if (orderby.contains("desc")) "desc" else "asc")

  override lazy val sqlcount = """select count(*) /*+ result_cache */ 
from enoread.manpartnerversion a,
enoread.manpartner p,
enovia.manpartmaster m,
enovia.manpartversion v
where a.vproject in (#PROJECTS#)
and a.vpartner = p.oid
and a.vmaster = m.oid
and m.oid = v.vmaster
and m.v514lastversion = v.oid
and #QUERY#
"""
    .replace("#QUERY#", query)

  class Row(result: RichResultSet) extends PartnerVersionDetail(
    row = result,
    id = result,
    name = result,
    alternatename = result,
    description_de = result,
    project = result,
    team = result,
    owner = result,
    lockstatus = result,
    lockuser = result,
    lastmodified = result,
    partner = result,
    master = result,
    version = result,
    versionstring = result,
    revisionstring = result,
    statusstring = result,
    releasedate = result)

  def partnermappings = Repository(classOf[Partners])

  override protected def prepare = statement << to << from

  override protected def row(result: RichResultSet) = new Row(result)

  override def doWrite(writer: PrintWriter) = {

    request.getMethod match {
      case Method.GET => {

        super.doWrite(writer)

      }
      case Method.POST =>
        try {
          writer.print("{\"response\":{\"data\":[")
          var rows = from
          transaction {
            doPost(writer)
            rows += 1
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
  }

  private def doPost(writer: PrintWriter) = {
    val timeout = 2 minutes
    val input = jsoninput.get

    val name = input.get("name").asString.trim
    val alternatename = input.get("alternatename").asString
    val versionstring = input.get("versionstring").asString
    val description_de = input.get("description_de").asString
    val owner = input.get("owner").asString
    val team = if (input.contains("team")) input.get("team").asString else "ADMIN"
    val project = if (input.contains("project")) input.get("project").asString else "DEFAULT"
    val created = new Timestamp(System.currentTimeMillis)
    var lastmodified = new Timestamp(System.currentTimeMillis)
    var releasedate: Option[Timestamp] = None
    var lockstatus = input.get("lockstatus").asString
    var lockuser = input.get("lockuser").asString
    var modifier = input.get("modifier").asString
    var modifierteam = input.get("modifierteam").asString
    var modifierproject = input.get("modifierproject").asString
    var revisionstring = input.get("revisionstring").asString
    var statusstring = input.get("statusstring").asString
    var changerequest = input.get("changerequest").asString
    var changerequestpartner = input.get("changerequestpartner").asString
    var validationcode = input.get("validationcode").asString
    var validationcodepartner = input.get("validationcodepartner").asString
    var cadtype = input.get("cadtype").asString
    var pda3d = input.get("pda3d").asString
    val partner = input.get("partner").asString
    val master = input.get("master").asString
    val version = input.get("version").asString

    val partnerversion = new PartnerVersionTable(
      name = name,
      owner = owner,
      team = team,
      project = project,
      partner = partner,
      master = master,
      version = version,
      releasedate = releasedate,
      lockstatus = Option(lockstatus),
      lockuser = Option(lockuser),
      versionstring = Option(versionstring),
      alternatename = Option(alternatename),
      description_de = Option(description_de),
      revisionstring = Option(revisionstring))

    try {
      partnerversions.insert(partnerversion)
    } catch {
      case e1: java.lang.Exception => {
        throw e1.getCause
      }
    } finally {
      writer.print(new PartnerVersionDetail(partnerversion))
    }

  }
}
