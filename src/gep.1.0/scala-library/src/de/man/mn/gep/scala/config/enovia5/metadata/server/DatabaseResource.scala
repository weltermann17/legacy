package de.man.mn.gep.scala.config.enovia5.metadata.server

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.Writer
import java.nio.channels.Channels
import java.nio.channels.WritableByteChannel

import scala.collection.JavaConversions._

import org.restlet.data.MediaType
import org.restlet.data.Method
import org.restlet.resource.Delete
import org.restlet.resource.Get
import org.restlet.resource.Post
import org.restlet.resource.Put
import org.restlet.resource.ServerResource
import org.restlet.Application
import org.restlet.Request

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichPreparedStatement
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.ps2Rich
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.resource.b
import com.ibm.de.ebs.plm.scala.resource.disposable
import com.ibm.de.ebs.plm.scala.resource.forceContextType
import com.ibm.de.ebs.plm.scala.resource.using
import com.ibm.de.ebs.plm.scala.rest.Services
import com.ibm.de.ebs.plm.scala.rest.WritableByteChannelRepresentation
import com.ibm.de.ebs.plm.scala.util.Io.nullstream
import com.ibm.de.ebs.plm.scala.util.Timers.time

abstract class DatabaseRepresentation
  extends WritableByteChannelRepresentation(MediaType.APPLICATION_JSON) {

  parent =>

  protected[server] def init(
    baseuri: String,
    connectionfactory: ConnectionFactory,
    parameters: Map[String, String],
    request: Request,
    mediatype: MediaType): DatabaseRepresentation = {

    setMediaType(mediatype)
    addDisposition

    this.parameters = parameters
    this.request = request

    this.baseuri = baseuri
    this.connectionfactory = connectionfactory

    query = parameters.getOrElse("com.ibm.de.ebs.plm.scala.rest.query.where", null)
    orderby = parameters.getOrElse("com.ibm.de.ebs.plm.scala.rest.query.orderby", null)
    from = parameters.getOrElse("com.ibm.de.ebs.plm.scala.rest.query.from", "0").toInt
    to = parameters.getOrElse("com.ibm.de.ebs.plm.scala.rest.query.to", "50").toInt

    this
  }

  protected[server] def init(json: String) = {
    jsoninput = Some(Json.parse(json).asObject)
    this
  }

  protected[server] def init(representation: org.restlet.representation.Representation) = {
    val form = new org.restlet.data.Form(representation)
    val json = form.getFirstValue(form.getNames.toList(0))
    jsoninput = Some(Json.parse(json).asObject)
    this
  }

  protected[server] def init(
    baseuri: String,
    connectionfactory: ConnectionFactory,
    parameters: Map[String, String]): DatabaseRepresentation = {
    init(baseuri, connectionfactory, parameters, new Request(Method.GET, ""), MediaType.APPLICATION_JSON)
  }

  protected[server] def addDisposition = ()

  protected lazy val (projectinlist, projectlist) = {
    if (isInstanceOf[Projects]) {
      ("''", List[String]())
    } else {
      if (DatabaseRepresentation.isCached(authorizationidentifier)) {
        DatabaseRepresentation.fromCache(authorizationidentifier)
      } else {
        val p = new Projects(parameters ++ Map("user" -> authorizationidentifier))(baseuri, connectionfactory)
        DatabaseRepresentation.cache(authorizationidentifier, p.toInList, p.toList)
      }
    }
  }

  protected def doWrite(write: PrintWriter): Unit = ()

  protected def doWrite(out: OutputStream): Unit = ()

  implicit lazy val authorizationidentifier = parameters("authorization-identifier")

  lazy val sql: String = null

  lazy val sqlcount: String = null

  lazy val count = {
    var result = 0
    val representation: DatabaseRepresentation = new DatabaseRepresentation {
      request = parent.request
      connectionfactory = parent.connectionfactory
      parameters = parent.parameters
      query = parent.query
      override lazy val sql = parent.sqlcount.replace("#PROJECTS#", projectinlist)
      override def doWrite(writer: PrintWriter) = {
        case class Count(count: Int)
        for (count <- parent.preparecount <<! (result => Count(result))) {
          result = count.count
        }
      }
    }
    representation.write(nullstream)
    result
  }

  protected[server] implicit val scheduler = de.man.mn.gep.scala.Server.getTaskService

  private def write(p: () => Unit) = {
    try {
      request.getMethod match {
        case Method.GET if sql != null =>
          val sql = this.sql.replace("#PROJECTS#", projectinlist)
          using {
            implicit val _ = forceContextType[Unit]
            val connection = disposable(connectionfactory.newConnection())
            val stmt = disposable(connection.prepareStatement(sql))
            statement = stmt
            val ms = time(p())
            println((if (60000 < ms) sql else "") + getClass.getSimpleName + " --> " + ms + " ms")
          }
        case Method.GET | Method.POST | Method.PUT | Method.DELETE =>
          (projectinlist, projectlist)
          using {
            implicit val _ = forceContextType[Unit]
            val connection = disposable(connectionfactory.newConnection())
            val ms = time(p())
            println(getClass.getSimpleName + " " + request.getMethod + " --> " + ms + " ms")
          }
      }
    } catch {
      case e =>
        e.printStackTrace
        throw e
    }
  }

  override def write(writer: Writer) = {
    write(() => doWrite(writer.asInstanceOf[PrintWriter]))
  }

  override def write(out: OutputStream) = getMediaType match {
    case MediaType.APPLICATION_MSOFFICE_XLSX =>
      write(() => doWrite(out))
      out.flush
    case MediaType.APPLICATION_JSON | _ =>
      val writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"))
      write(writer)
      writer.flush
  }

  override def write(out: WritableByteChannel) = {
    write(Channels.newOutputStream(out))
  }

  override def release = {
    request = null
    super.release
  }

  lazy val datatype = baseuri match {
    case url if baseuri.contains("/versions/") => "versions"
    case url if baseuri.contains("/instances/") => "instances"
    case url if baseuri.contains("/products/") => "products"
    case url if baseuri.contains("/snapshots/") => "snapshots"
    case url if baseuri.contains("/partnerversions/") => "partnerversions"
  }

  protected def preparecount(implicit statement: RichPreparedStatement): RichPreparedStatement = statement

  protected def computeTotal(rows: Int) = {
    if (rows < to && from < rows) rows else if (pagesize == rows && 0 == from) pagesize + 1 else if (pagesize == from) count else -1
  }

  protected implicit val _ = this
  protected implicit var statement: RichPreparedStatement = null
  protected[server] implicit var connectionfactory: ConnectionFactory = null
  protected[server] var baseuri: String = null
  protected implicit var parameters: Map[String, String] = null
  protected val pagesize = 50
  protected var query: String = null
  protected var orderby: String = null
  protected var from: Int = 0
  protected var to: Int = pagesize
  protected var jsoninput: Option[Json.JObject] = None
  protected var request: Request = null

}

object DatabaseRepresentation {

  def isCached(identifier: String) = {
    projectsmappings.containsKey(identifier.toUpperCase)
  }

  def cache(identifier: String, inlist: String, list: List[String]) = {
    projectsmappings.put(identifier.toUpperCase, (inlist, list))
    (inlist, list)
  }

  def fromCache(identifier: String) = {
    projectsmappings.get(identifier.toUpperCase)
  }

  private val projectsmappings = new java.util.concurrent.ConcurrentHashMap[String, (String, List[String])]

}

case class DatabaseResource[D <: DatabaseRepresentation](
  baseuri: String,
  parameters: Map[String, String],
  connectionfactory: ConnectionFactory,
  representationclass: Class[D])
  extends ServerResource {

  @Get
  @Delete
  def handleAll: DatabaseRepresentation = {
    val representation = representationclass.newInstance.asInstanceOf[D]
    representation.init(baseuri, connectionfactory, parameters, getRequest, Services.getPreferredMediaType(getRequest))
  }

  @Post("json")
  @Put("json")
  def handleJsonInput(json: String): DatabaseRepresentation = {
    handleAll.init(json)
  }

  @Post
  @Put
  def handleFormInput(representation: org.restlet.representation.Representation): DatabaseRepresentation = {
    handleAll.init(representation)
  }
}

