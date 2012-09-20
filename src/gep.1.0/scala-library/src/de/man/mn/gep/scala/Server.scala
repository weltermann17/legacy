package de.man.mn.gep.scala

import java.net.URL
import java.util.logging.Level
import org.restlet.data.ChallengeResponse
import org.restlet.engine.Engine
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2Array
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2Double
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2Int
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2Long
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2Object
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2String
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.rest.CachingRepresentations
import com.ibm.de.ebs.plm.scala.rest.Encoding
import com.ibm.de.ebs.plm.scala.rest.Expires
import com.ibm.de.ebs.plm.scala.rest.Files
import com.ibm.de.ebs.plm.scala.rest.Redirections
import com.ibm.de.ebs.plm.scala.rest.RepresentationDirectoryCache
import com.ibm.de.ebs.plm.scala.rest.ServerComponent
import com.ibm.de.ebs.plm.scala.rest.StringFunctionFilter
import com.ibm.de.ebs.plm.scala.rest.TextFilter
import com.ibm.de.ebs.plm.scala.util.Io.buffersize
import com.ibm.de.ebs.plm.scala.util.Timers.Int2Unit
import com.ibm.de.ebs.plm.scala.util.Timers.Int2Value
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository
import de.man.mn.gep.scala.config.enovia5.metadata.server.Enovia5ConnectionFactory
import de.man.mn.gep.scala.config.enovia5.vault.HttpVault
import de.man.mn.gep.scala.config.user.Users
import de.man.mn.gep.scala.config.Layouts
import de.man.mn.gep.scala.config.Models
import de.man.mn.gep.scala.config.Plm
import com.ibm.de.ebs.plm.scala.rest.Expires
import de.man.mn.gep.scala.config.Plm
import com.ibm.de.ebs.plm.scala.rest.StringFunctionFilter

object Server extends ServerComponent {

  def main(args: Object): Unit = {
    setSystemProperties
    setServers
    setClients
    wardirectory = System.getProperty("de.man.mn.gep.war.directory", null)
    configfiles = args.asInstanceOf[Array[String]].foldLeft(List[URL]()) { case (l, a) => new URL(a.replace(" ", "%20")) :: l }
    bootstrap
  }

  private def setSystemProperties = {
    System.setProperty("org.restlet.engine.io.bufferSize", buffersize.toString);
    System.setProperty("org.restlet.engine.io.timeoutMs", (30 * 1000).toString);
  }

  private def setServers = {
    val servers = Engine.getInstance.getRegisteredServers
    servers.clear
    servers.add(new org.restlet.ext.jetty.HttpServerHelper(null))
    servers.add(new org.restlet.engine.local.RiapServerHelper(null))
    servers.add(new org.restlet.engine.connector.HttpServerHelper(null))
  }

  private def setClients = {
    val clients = Engine.getInstance.getRegisteredClients
    clients.clear
    clients.add(new org.restlet.ext.httpclient.HttpClientHelper(null))
    clients.add(new org.restlet.engine.connector.HttpClientHelper(null))
    clients.add(new org.restlet.engine.local.ClapClientHelper(null))
    clients.add(new org.restlet.engine.local.RiapClientHelper(null))
    clients.add(new org.restlet.engine.local.FileClientHelper(null))
    clients.add(new org.restlet.engine.local.ZipClientHelper(null))
  }

  val version = "gep-1.2.0.revision." + revision.substring(6).replace("$", "") + "(" + buildtime.substring(7, 26) + ") - " + author.substring(9).replace("$", "")

  def domain = get("de.man.mn.gep.host.domain")
  def ports: List[Int] = get("de.man.mn.gep.host.ports").asArray.map { port => port.asInt }

  lazy val ApplicationPort: Int = get("de.man.mn.gep.application.port").asInt
  lazy val LocalPorts: List[Int] = get("de.man.mn.gep.vault.local.ports").asArray.foldLeft(List[Int]()) { case (l, e) => e :: l }
  lazy val CachePorts: List[Int] = get("de.man.mn.gep.vault.cache.ports").asArray.foldLeft(List[Int]()) { case (l, e) => e :: l }
  lazy val Enovia5DatabasePort: Int = get("de.man.mn.gep.enovia5.port").asInt

  object ServerType extends Enumeration {
    type ServerType = Value
    val Application, Local, Cache, Enovia5Database, Invalid = Value
  }
  import ServerType._

  lazy val servertype = {
    val port = ports(0)
    if (ApplicationPort == port)
      Application
    else if (LocalPorts.contains(port))
      Local
    else if (CachePorts.contains(port))
      Cache
    else if (Enovia5DatabasePort == port)
      Enovia5Database
    else
      Invalid
  }

  def applicationResource(uri: String, authorization: ChallengeResponse) = {
    val hostref = try { "http://" + get("de.man.mn.gep.appserver").asString } catch { case _ => Configuration.applicationHost }
    new org.restlet.resource.ClientResource(childContext, new org.restlet.data.Reference(hostref + uri)) {
      getReference.setHostPort(ApplicationPort)
      setChallengeResponse(authorization)
      setRetryAttempts(1)
      setRetryDelay(500)
    }
  }

  def currentConfiguration(parameters: Map[String, String], authorization: ChallengeResponse) = {
    Configuration(parameters, authorization)
  }

  override def preBootstrapping = {
    setEnvironment(configuration)
    initializeServers(get("de.man.mn.gep.server.connectors"))
    initializeClients(get("de.man.mn.gep.client.connectors"))
  }

  override def postBootstrapping {
    logger.setLevel(Level.parse(get("de.man.mn.gep.logger.level").toUpperCase))

    val context = childContext

    val directorymaxsize = get("de.man.mn.gep.cache.directory.maxsize")
    val directory = if (0 < directorymaxsize) RepresentationDirectoryCache(
      get("de.man.mn.gep.cache.directory.url"),
      directorymaxsize,
      get("de.man.mn.gep.cache.directory.shrinkby"),
      15 minutes,
      context)
    else null

    servertype match {
      case Application =>
        attach("/",
          Encoding(
            Expires(
              Files(
                if (null == wardirectory) "clap://thread/war" else wardirectory,
                context),
              1 week,
              context),
            context))

        attach("/redirect",
          Expires(
            CachingRepresentations(
              directory,
              Redirections(get("de.man.mn.gep.redirections").asObject,
                context),
              1 week,
              context),
            1 week,
            context))

        attach("/static",
          Encoding(
            Expires(
              TextFilter(
                "/static",
                Json(Map("/configuration/" -> Json(Json.build(env)))
                  ++ Map("/routing/" -> Json(routing))
                  ++ Map("/layout/searchresult/versions/" -> Json(Layouts.SearchResults.Versions))
                  ++ Map("/layout/searchresult/bom/" -> Json(Layouts.SearchResults.Bom))
                  ++ Map("/layout/searchresult/whereused/" -> Json(Layouts.SearchResults.WhereUsed))
                  ++ Map("/layout/searchresult/instances/" -> Json(Layouts.SearchResults.Instances))
                  ++ Map("/layout/searchresult/products/" -> Json(Layouts.SearchResults.Products))
                  ++ Map("/layout/searchresult/partners/" -> Json(Layouts.SearchResults.Partners))
                  ++ Map("/layout/searchresult/partnerversions/" -> Json(Layouts.SearchResults.PartnerVersions))
                  ++ Map("/layout/searchresult/snapshots/" -> Json(Layouts.SearchResults.Snapshots))
                  ++ Map("/layout/details/versions/" -> Json(Layouts.Details.Versions))
                  ++ Map("/layout/details/bom/" -> Json(Layouts.Details.Versions))
                  ++ Map("/layout/details/whereused/" -> Json(Layouts.Details.Versions))
                  ++ Map("/layout/details/instances/" -> Json(Layouts.Details.Instances))
                  ++ Map("/layout/details/products/" -> Json(Layouts.Details.Products))
                  ++ Map("/layout/details/partners/" -> Json(Layouts.Details.Partners))
                  ++ Map("/layout/details/partnerversions/" -> Json(Layouts.Details.PartnerVersions))
                  ++ Map("/layout/details/snapshots/" -> Json(Layouts.Details.Snapshots))
                  ++ Map("/layout/menus/versions/" -> Json(Layouts.Menus.Versions))
                  ++ Map("/layout/menus/bom/" -> Json(Layouts.Menus.Versions))
                  ++ Map("/layout/menus/whereused/" -> Json(Layouts.Menus.Versions))
                  ++ Map("/layout/menus/instances/" -> Json(Layouts.Menus.Instances))
                  ++ Map("/layout/menus/products/" -> Json(Layouts.Menus.Products))
                  ++ Map("/layout/menus/partners/" -> Json(Layouts.Menus.Partners))
                  ++ Map("/layout/menus/partnerversions/" -> Json(Layouts.Menus.PartnerVersions))
                  ++ Map("/layout/menus/snapshots/" -> Json(Layouts.Menus.Snapshots))
                  ++ Map("/configuration/location/" -> Json("[\"" + get("de.man.mn.gep.enovia5.vault.this.location").asString + "\"]"))
                  ++ Map("/model/plmservers/" -> Json(Models.PlmServers))
                  ++ Map("/model/search/" -> Json(Models.Search))
                  ++ Map("/model/searchpref/" -> Json(Models.SearchPref)))),
              1 week,
              context),
            context))

        attach("/plm",
          Encoding(
            Plm("/plm", directory, initializeDatabases).apply,
            context))

        attach("/users", Users(directory, context).apply)

      case Local =>
        attach("/plm",
          Plm("/plm", directory, initializeDatabases).apply)

        attach("/VGV",
          new HttpVault().apply)

      case Cache =>
        attach("/plm",
          Plm("/plm", directory, initializeDatabases).apply)

        attach("/VGV",
          new HttpVault().apply)

      case Enovia5Database =>
        attach("/plm",
          Encoding(
            Plm("/plm", directory, initializeDatabases).apply,
            context))

      case _ => throw new Exception("Invalid server type.")
    }

    attach("/shutdown/",
      StringFunctionFilter(shutdown))

    attach("/ping/",
      StringFunctionFilter(ping))

    logger.info(servertype.toString)
    logger.info(version)

    start
    startDatabases

    de.man.mn.gep.scala.config.enovia5.metadata.inmemory.partner.PartnerSchema.test

  }

  lazy implicit val initializeDatabases = servertype match {
    case Cache | Enovia5Database => new Enovia5ConnectionFactory
    case _ => null
  }

  def startDatabases = {
    servertype match {
      case Cache | Enovia5Database =>
        initializeDatabases.init
      case _ =>
    }
    servertype match {
      case Enovia5Database =>
        Repository.bootstrap(1 second, 2 hours)
      case _ =>
    }
  }

  lazy val configuration = {
    var duplicate: Option[String] = None
    Json.parse(configstring).asArray.toList.foldLeft(Map[String, Json]()) {
      case (m, e) =>
        if (!e.filterKeys { k =>
          logger.finest(k + " processed.")
          if (m.get(k).isDefined) {
            if (duplicate.isEmpty) duplicate = Some(k)
            true
          } else false
        }.isEmpty)
          throw new IndexOutOfBoundsException("Configuration key duplicated: " + duplicate.get)
        m ++ e.asObject
    }
  }

  lazy val configstring = {
    val buf = new StringBuilder
    buf.append("[")
    var i = 0
    configfiles.foreach { f =>
      if (0 < i) buf.append(",");
      buf.append(io.Source.fromURL(f, "UTF8").mkString)
      i += 1
      logger.fine(f + " read.")
    }
    buf.append("]")
    buf.toString
  }

  var wardirectory: String = null
  var configfiles: List[URL] = null

}

