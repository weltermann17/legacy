package com.ibm.de.ebs.plm.scala.rest

import org.restlet.data.CharacterSet
import org.restlet.data.Language
import org.restlet.data.MediaType
import org.restlet.data.Status
import org.restlet.representation.StringRepresentation
import org.restlet.resource.Get
import org.restlet.resource.ServerResource
import org.restlet.routing.Filter
import org.restlet.Request
import org.restlet.Response
import org.restlet.Restlet

import com.ibm.de.ebs.plm.scala.json.Json

trait AttributesConverter extends Restlet {

  def convert(s: String): String

  val parameternames: List[String]

  abstract override def handle(request: Request, response: Response): Unit = {
    val attr = request.getAttributes
    parameternames.foreach { n =>
      val v = attr.get(n) match { case null => case v => attr.put(n, convert(v.toString)) }
    }
    super.handle(request, response)
  }
}

case class FunctionFilter(p: Map[String, String] => String, parameternames: String*)(implicit neverreached: Class[_ <: ServerResource] = classOf[NeverReached]) extends Filter {

  override protected def beforeHandle(request: Request, response: Response) = {
    def parameters = {
      parameternames.foldLeft(Map[String, String]()) { (m, n) =>
        val v = request.getAttributes.get(n) match { case null => "" case v => v.toString }
        m ++ Map(n -> v)
      }
    }
    val representation = new StringRepresentation(p(parameters), MediaType.APPLICATION_JSON, Language.ALL, CharacterSet.UTF_8)
    response.setEntity(representation)
    Filter.STOP
  }

  setNext(neverreached)
}

case class StringFunctionFilter(p: () => String)(implicit neverreached: Class[_ <: ServerResource] = classOf[NeverReached]) extends Filter {

  override protected def beforeHandle(request: Request, response: Response) = {
    val representation = new StringRepresentation(p(), MediaType.APPLICATION_JSON, Language.ALL, CharacterSet.UTF_8)
    response.setEntity(representation)
    Filter.STOP
  }

  setNext(neverreached)
}

case class TextFilter(base: String, mapping: Json.JObject)(implicit neverreached: Class[_ <: ServerResource] = classOf[NeverReached]) extends Filter {

  private val map = mapping.foldLeft(Map[String, Json]()) { (m, e) => m ++ Map(base + e._1 -> e._2) }

  private def key(request: Request) = request.getResourceRef.getPath

  override protected def beforeHandle(request: Request, response: Response) = map.get(key(request)) match {
    case Some(v) =>
      val representation = new StringRepresentation(v.asString, MediaType.APPLICATION_JSON, Language.ALL, CharacterSet.UTF_8)
      response.setEntity(representation)
      Filter.STOP
    case None =>
      response.setStatus(Status.valueOf(404));
      Filter.STOP
  }

  setNext(neverreached)
}

case class TextResource(text: String) extends ServerResource {

  @Get
  def doGet = {
    val representation = new StringRepresentation(text, MediaType.APPLICATION_JSON, Language.ALL, CharacterSet.UTF_8)
    representation
  }
}

private[rest] class NeverReached extends TextResource("{}")
