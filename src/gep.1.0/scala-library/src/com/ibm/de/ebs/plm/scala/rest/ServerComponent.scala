package com.ibm.de.ebs.plm.scala.rest

import java.util.logging.Level
import java.util.LinkedList
import java.util.TreeSet

import org.restlet.data.Protocol
import org.restlet.routing.Router
import org.restlet.routing.TemplateRoute
import org.restlet.routing.VirtualHost
import org.restlet.Restlet
import org.restlet.Application
import org.restlet.Component
import org.restlet.Context

import collection.JavaConversions._

import com.ibm.de.ebs.plm.scala.concurrent.ops.schedule
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Any2Json
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2Int
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2String
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.util.Timers.Int2Unit
import com.ibm.de.ebs.plm.scala.util.Timers.Int2Value

trait ServerComponent extends Component with ChildContext { component =>
  def domain: String
  def ports: List[Int]

  def preBootstrapping

  def postBootstrapping

  def bootstrap: Unit = {
    preBootstrapping

    ports.foreach { port =>
      val virtualhost = new VirtualHost(getContext)
      virtualhost.setHostDomain(domain)
      virtualhost.setHostPort(port.toString)
      getHosts.add(virtualhost)
    }

    postBootstrapping
  }

  protected val statusservice = new StatusService
  setStatusService(statusservice)

  protected implicit val taskservice = new TaskService(512)

  def getTaskService = taskservice

  def attach(uri: String, restlet: Restlet): Application = {
    val application = new Application(childContext) {
      setTaskService(taskservice)
      setStatusService(statusservice)
      getStatusService.setEnabled(true)
      getConnectorService.setClientProtocols(clientprotocols)
      getDecoderService.setEnabled(false)
      getTunnelService.setEnabled(false)

      override def createInboundRoot = restlet
    }
    getHosts.foreach { virtualhost =>
      virtualhost.attach(uri, application).setMatchingMode(Router.MODE_BEST_MATCH)
    }
    application
  }

  def get(name: String): Json = System.getProperty(name.toLowerCase) match {
    case null => env.get(name.toLowerCase) match { case Some(v) => v case None => throw new Exception("Not found: " + name.toLowerCase) }
    case v => v
  }

  lazy val logger = getLogger

  protected def setEnvironment(env: Json.JObject) = {
    put(env)
    if (logger.isLoggable(Level.FINE)) {
      val buf = new StringBuilder
      val sorted = new TreeSet[String](env.keySet)
      sorted.foreach { k =>
        buf.append(k).append(" -> ").append(get(k).toString).append("\n")
      }
      logger.fine(buf.toString)
    }
  }

  protected def initializeServers(servers: Json.JArray) = servers.foreach { s =>
    try {
      val server = getServers.add(Protocol.valueOf(s.asObject("protocol").toUpperCase), s.asObject("port"))
      val alias = s.asObject.get("parameters").asString.substring(1)
      get(alias).asObject.foreach {
        case (n, v) =>
          println(server.getClass + ": " + n + " -> " + v)
          server.getContext.getParameters.add(n, v.toString)
      }
    } catch {
      case _ =>
    }
  }

  protected def initializeClients(clients: Json.JArray) = {
    clients.foreach { c =>
      val protocol = Protocol.valueOf(c.asObject("protocol").toUpperCase)
      val client = getClients.add(protocol)
      clientprotocols.add(protocol)
      c.asObject.get("parameters").asObject.foreach {
        case (n, v) =>
          println(client.getClass + ": " + n + " -> " + v)
          client.getContext.getParameters.add(n, v.toString)
      }
    }
    getContext.getClientDispatcher.setProtocols(clientprotocols)
  }

  protected lazy val routing = {
    def getRoutes(restlet: Restlet, level: Int): Unit = {
      var i = 0
      restlet match {
        case router: Router =>
          router.getRoutes.foreach { r =>
            i += 1; buffer.append(i + "/" + router.getRoutes.size + " : ")
            (0 until level).foreach { _ => buffer.append(" -> ") }
            buffer.append(r.asInstanceOf[TemplateRoute].getTemplate.getPattern)
            buffer.append(" -> ")
            buffer.append(r.getNext.toString.replace(",", ",\n"))
            buffer.append("\n\n")
            getRoutes(r.getNext, level + 1)
          }
        case filter: org.restlet.routing.Filter =>
          buffer.append(filter.toString.replace(",", ",\n"))
          buffer.append("\n\n")
          getRoutes(filter.getNext, level + 1)
        case a: Application => getRoutes(a.getInboundRoot, level)
        case null =>
        case e =>
          buffer.append(e.toString.replace(",", ",\n"))
          buffer.append("\n\n")
      }
    }
    lazy val buffer = new StringBuilder(16 * 1024)
    getHosts.foreach { virtualhost =>
      getRoutes(virtualhost, 0)
    }
    buffer.toString
  }

  protected val ping = () => {
    Json.build(Map(
      "status" -> 0,
      "message" -> (getDefaultHost.getName + " is alive."),
      "pingcount" -> pingcounter.incrementAndGet))
  }

  protected val shutdown = () => {
    schedule(1 second) { stop; schedule(2 seconds) { Runtime.getRuntime.exit(-1) } }
    ping()
  }

  private val modifieddummy = 1;
  protected val revision = "$Rev: 4274 $"
  protected val buildtime = "$Date: 2011-10-18 12:51:18 +0200 (Tue, 18 Oct 2011) $"
  protected val author = "$Author: u62xz $"
  protected lazy val env: Json.JObject = getContext.getAttributes.get("com.ibm.de.ebs.plm.scala.rest.environment").asInstanceOf[Json.JObject]

  private def put(env: Json.JObject) = getContext.getAttributes.put("com.ibm.de.ebs.plm.scala.rest.environment", env)
  private val clientprotocols = new LinkedList[Protocol]
  private val pingcounter = new java.util.concurrent.atomic.AtomicInteger
}

trait ChildContext { this: Restlet =>

  def childContext: Context = {
    val parent = getContext
    val child = parent.createChildContext
    child.setAttributes(parent.getAttributes)
    child.setLogger(parent.getLogger)
    child.setParameters(parent.getParameters)
    child.setClientDispatcher(parent.getClientDispatcher)
    child.setDefaultEnroler(parent.getDefaultEnroler)
    child.setDefaultVerifier(parent.getDefaultVerifier)
    child.setServerDispatcher(parent.getServerDispatcher)
    child
  }

}
