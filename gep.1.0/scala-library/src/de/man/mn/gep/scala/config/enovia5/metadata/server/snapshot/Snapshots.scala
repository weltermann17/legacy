package de.man.mn.gep.scala.config.enovia5.metadata.server.snapshot

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

import SnapshotSchema.snapshot2iteration
import SnapshotSchema.snapshots
import de.man.mn.gep.scala.config.enovia5.metadata.server.ResultSetRepresentation

class Snapshots extends ResultSetRepresentation {

  override lazy val sql = """select /*+ result_cache */ /*+ first_rows(50) */ 
rnum,
vname,
vdescriptionde,
vbytesstored,
viterationstagged,
vparentname,
rawtohex(vparent),
vparenttype,
vuser,
lockstatus,
lockuser,
cexpirationdate,
cmodified,
rawtohex(oid)
from (
select t.*, rownum rnum from (
select a.*, #ORDERBYALIAS# 
from enoread.mansnapshot a 
where a.vproject in (#PROJECTS#) 
and cexpirationdate > sysdate
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
from enoread.mansnapshot a
where a.vproject in (#PROJECTS#) 
and #QUERY#
"""
    .replace("#QUERY#", query)

  class Row(result: RichResultSet) extends SnapshotDetail(
    row = result,
    name = result,
    description_de = result,
    bytesstored = result,
    iterationstagged = result,
    parentname = result,
    parentoid = result,
    parenttype = result,
    owner = result,
    lockstatus = result,
    lockuser = result,
    expirationdate = result,
    lastmodified = result,
    id = result,
    created = None,
    team = None,
    project = None)

  override protected def prepare = statement << to << from

  override protected def row(result: RichResultSet) = new Row(result)

  override def doWrite(writer: PrintWriter) = {
    request.getMethod match {
      case Method.GET => super.doWrite(writer)
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
          case e =>
            writer.print("],\"status\":-1}}")
            throw e
        }
    }
  }

  private def doPost(writer: PrintWriter) = {
    val timeout = 15 minutes
    val input = jsoninput.get
    val name = input.get("name").asString
    val description_de = input.get("description_de").asString
    val owner = input.get("owner").asString
    val team = if (input.contains("team")) input.get("team").asString else "ADMIN"
    val project = if (input.contains("project")) input.get("project").asString else "DEFAULT"
    val parenttype = input.get("parenttype").asString
    if (parenttype == "instances") {
      val uri = input.get("bom").asString
      val b = uri.indexOf("versions/") + ("versions/").length
      val e = uri.asString.indexOf("/", b)
      versionid = uri.substring(b, e)
    }
    val datatype = if ("instances" == parenttype) "versions" else parenttype
    val parentoid = input.get("parentoid").asString
    val parentname = input.get("parentname").asString
    val expirationdate = input.get("expirationdate").asString
    val assembly = getJsonAsByteArray(input.get("assembly").asString, parenttype)
    val bom = getJsonAsByteArray(input.get("bom").asString + "&from=0&to=" + Int.MaxValue, datatype)
    val whereused = getJsonAsByteArray(input.get("whereused").asString + "&from=0&to=" + Int.MaxValue, datatype)
    val spacetree = getJsonAsByteArray(input.get("spacetree").asString, datatype)
    val millertree = getJsonAsByteArray(input.get("millertree").asString, datatype)
    val formatsdetails = getJsonAsByteArray(input.get("formatsdetails").asString, parenttype)
    val formatssummary = getJsonAsByteArray(input.get("formatssummary").asString, parenttype)
    val iterations = getIterations(input.get("iterations").asString, datatype)

    val total = assembly.get(timeout).length +
      spacetree.get(timeout).length +
      millertree.get(timeout).length +
      formatsdetails.get(timeout).length +
      formatssummary.get(timeout).length +
      bom.get(timeout).length +
      whereused.get(timeout).length

    val snapshot = new SnapshotTable(
      owner = owner,
      description_de = Some(description_de),
      team = team,
      project = project,
      name = name,
      expirationdate = new java.sql.Timestamp(new SimpleDateFormat("yyyy-MM-dd").parse(expirationdate).getTime),
      bytesstored = total,
      iterationstagged = iterations.get(timeout).size,
      parenttype = parenttype,
      parentoid = parentoid,
      parentname = parentname,
      bom = bom.get,
      whereused = whereused.get,
      assembly = assembly.get,
      spacetree = spacetree.get,
      millertree = millertree.get,
      formatsdetails = formatsdetails.get,
      formatssummary = formatssummary.get)

    snapshots.insert(snapshot)

    val relations = new scala.collection.mutable.ListBuffer[Snapshot2Iteration]
    iterations.get.foreach { iteration =>
      relations += new Snapshot2Iteration(snapshot.id, iteration.asString)
    }
    snapshot2iteration.insert(relations)

    writer.print(new SnapshotDetail(snapshot))
  }

  private def getJsonAsByteArray(uri: String, datatype: String): Future[Array[Byte]] = {
    future {
      var spacetree = false
      val resource = {
        if (uri.contains("/assembly/")) datatype match {
          case "versions" | "products" => new de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.AssemblyInMemory
          case "instances" => new de.man.mn.gep.scala.config.enovia5.metadata.server.instance.InstanceAssembly
        }
        else if (uri.contains("/spacetree/")) {
          spacetree = true; datatype match {
            case "versions" | "instances" => new de.man.mn.gep.scala.config.enovia5.metadata.server.version.SpaceTree
            case "products" => new de.man.mn.gep.scala.config.enovia5.metadata.server.product.SpaceTree
          }
        } else if (uri.contains("/millertree/")) datatype match {
          case "versions" | "instances" => new de.man.mn.gep.scala.config.enovia5.metadata.server.version.MillerTree
          case "products" => new de.man.mn.gep.scala.config.enovia5.metadata.server.product.MillerTree
        }
        else if (uri.contains("/bom/")) datatype match {
          case "versions" | "instances" => new de.man.mn.gep.scala.config.enovia5.metadata.server.version.Bom
          case "products" => new de.man.mn.gep.scala.config.enovia5.metadata.server.product.Bom
        }
        else if (uri.contains("/whereused/")) datatype match {
          case "versions" | "instances" => new de.man.mn.gep.scala.config.enovia5.metadata.server.version.WhereUsed
          case "products" => null
        }
        else if (uri.contains("/formats/details/")) new de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document.FormatsDetailsInMemory
        else if (uri.contains("/formats/summary/")) new de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document.FormatsSummaryInMemory
        else null
      }
      val bytearrayoutputstream = new ByteArrayOutputStream(buffersize)
      val gzipoutputstream = new GZIPOutputStream(bytearrayoutputstream, buffersize)
      if (null != resource) {
        val (template, id) = mapUri(uri, datatype)
        val rparameters = parameters ++ Map(
          datatype.substring(0, datatype.length - 1) -> id,
          "node" -> (if (spacetree) "1" else "0"),
          "partner" -> "E137A1C70221436FB881EE2773787EE2",
          "com.ibm.de.ebs.plm.scala.rest.query.where" -> "",
          "com.ibm.de.ebs.plm.scala.rest.query.orderby" -> "",
          "com.ibm.de.ebs.plm.scala.rest.query.from" -> "0",
          "com.ibm.de.ebs.plm.scala.rest.query.to" -> "2147483647") ++ (
            if ("instances" == datatype) {
              Map("version" -> versionid)
            } else {
              Map()
            })
        resource.init(template, connectionfactory, rparameters)
        resource.write(gzipoutputstream)
      } else {
        val writer = new PrintWriter(new OutputStreamWriter(gzipoutputstream, "UTF-8"))
        writer.print("{}")
        writer.flush
      }
      gzipoutputstream.finish
      gzipoutputstream.close
      val b = bytearrayoutputstream.toByteArray
      println(uri + " " + b.length)
      b
    }
  }

  private def getIterations(uri: String, datatype: String): Future[Json.JArray] = {
    future {
      val iterations = new de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document.IterationsInMemory
      val (template, id) = mapUri(uri, datatype)
      iterations.init(template, connectionfactory, parameters ++ Map(datatype.substring(0, datatype.length - 1) -> id))
      val stringwriter = new java.io.StringWriter(buffersize)
      iterations.write(new PrintWriter(stringwriter))
      val result = Json.parse(stringwriter.toString).asObject.get("response").asObject.get("data").asArray
      result
    }
  }

  private def mapUri(uri: String, datatype: String) = {
    val b = uri.indexOf(datatype + "/") + (datatype + "/").length
    val e = uri.indexOf("/", b)
    (uri.substring(0, b) + "{" + datatype.substring(0, datatype.length - 1) + "}" + uri.substring(e), uri.substring(b, e))
  }

  private var versionid: String = ""

}
