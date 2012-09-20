package com.ibm.de.ebs.plm.scala.rest

import java.io.PrintWriter
import java.lang.OutOfMemoryError
import java.lang.Runtime

import org.restlet.data.Reference
import org.restlet.data.Status
import org.restlet.resource.Finder
import org.restlet.resource.Directory
import org.restlet.resource.Resource
import org.restlet.resource.ServerResource
import org.restlet.routing.Filter
import org.restlet.service.EncoderService
import org.restlet.Context
import org.restlet.Request
import org.restlet.Response
import org.restlet.Restlet

case class Files(url: String, context: Context) extends Directory(context, url) {
  setListingAllowed(false)
  setModifiable(false)
  setNegotiatingContent(false)

  override def find(request: Request, response: Response) = {
    if (getRootRef.toString.startsWith("clap")) {
      val resourceref = request.getResourceRef.toString
      if (resourceref.endsWith("/")) {
        request.setResourceRef(resourceref + "index.html")
      }
    }
    super.find(request, response)
  }

}

case class Encoding(next: Restlet, context: Context)
  extends org.restlet.engine.application.Encoder(context, false, true, new EncoderService) {
  setNext(next)
}

case class LogURIFilter(next: Restlet, context: Context, writer: PrintWriter) extends Filter(context, next) {
  override protected def beforeHandle(request: Request, response: Response) = {
    if (null != writer) {
      writer.println(request.getOriginalRef.toString)
    }
    Filter.CONTINUE
  }
}

class StatusService extends org.restlet.service.StatusService(true) {

  super.setOverwriting(true)

  override def getRepresentation(status: Status, request: Request, response: Response) = {
    println("StatusService : getRepresentation : status = " + status)
    super.getRepresentation(status, request, response)
  }

  override def getStatus(throwable: Throwable, resource: Resource): Status = {
    println("StatusService : getStatus(throwable: Throwable, resource: Resource)")
    getStatus(throwable, resource.getRequest, resource.getResponse)
  }

  override def getStatus(throwable: Throwable, request: Request, response: Response): Status = {
    errorcounter.incrementAndGet
    println("StatusService : errorcounter = " + errorcounter.get)
    println("StatusService : " + throwable)
    throwable match {
      case e: OutOfMemoryError =>
        println("StatusService : Out of memory. Program will be aborted now.")
        Runtime.getRuntime.exit(-1)
      case e =>
        // throwable.printStackTrace
        println("StatusService : " + e)
    }
    if (maxerrors < errorcounter.get) {
      println("StatusService : Too many errors. Program will be aborted now.")
      Runtime.getRuntime.exit(-2)
    }
    Status.SERVER_ERROR_INTERNAL
  }

  override def getHomeRef = {
    new Reference("/")
  }

  def getErrorCount = errorcounter.get

  private val maxerrors = 1000
  private val errorcounter = new java.util.concurrent.atomic.AtomicLong
}

trait HasParameters {

  this: Finder =>

  val uritemplate: String

  def parameternames = """\{\w+\}""".r.findAllIn(uritemplate).toList.map { e => e.substring(1, e.length - 1) }

  override def create(request: org.restlet.Request, response: org.restlet.Response): ServerResource = {
    val parameters = {
      parameternames.foldLeft(Map[String, String]()) { (m, n) => request.getAttributes.get(n) match { case null => m case v => m ++ (Map(n -> v.toString)) } }
    }
    create(request, response, parameters)
  }

  def create(request: org.restlet.Request, response: org.restlet.Response, parameters: Map[String, String]): ServerResource

}
