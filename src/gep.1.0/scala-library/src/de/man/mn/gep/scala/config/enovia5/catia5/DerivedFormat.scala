package de.man.mn.gep.scala.config.enovia5.catia5

import scala.collection.immutable.Map

import org.restlet.data.MediaType
import org.restlet.data.Method
import org.restlet.representation.EmptyRepresentation
import org.restlet.representation.Representation
import org.restlet.resource.Get
import org.restlet.resource.Post
import org.restlet.resource.Finder
import org.restlet.resource.ServerResource

import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.resource.c
import com.ibm.de.ebs.plm.scala.resource.disposable
import com.ibm.de.ebs.plm.scala.resource.forceContextType
import com.ibm.de.ebs.plm.scala.resource.using
import com.ibm.de.ebs.plm.scala.rest.HasParameters
import com.ibm.de.ebs.plm.scala.rest.WritableByteChannelRepresentation
import com.ibm.de.ebs.plm.scala.util.Io.addDisposition

import de.man.mn.gep.scala.Server

class DerivedFormat(derive: Derive, parameters: Map[String, String])
  extends WritableByteChannelRepresentation(MediaType.APPLICATION_ZIP) {

  override def write(out: java.io.OutputStream) = {
    implicit val _ = forceContextType[Unit]
    using {
      derive.out = out
      val zipout = disposable(derive.zipout)
      derive.retrieveAssembly
      derive.writeManifest
      derive.writeLocations
      derive.writeAssembly
      derive.writeConfig
    }
  }

}

case class DerivedFormatFinder(uritemplate: String) extends Finder with HasParameters {

  override def create(request: org.restlet.Request, response: org.restlet.Response, parameters: Map[String, String]) = {
    DerivedFormatResource(parameters ++ Map("authorization-identifier" -> request.getChallengeResponse.getIdentifier.toUpperCase))
  }

  case class DerivedFormatResource(parameters: Map[String, String]) extends ServerResource {

    @Get
    def doGet = {
      try {
        getMethod match {
          case Method.HEAD =>
            new EmptyRepresentation
          case _ =>
            val derivedformat = new DerivedFormat(derive, parameters)
            getResponse.setEntity(derivedformat)
            addDisposition(derivedformat, parameters, "derivedformat")
        }
      } catch {
        case e => e.printStackTrace()
      }
    }

    @Post("json")
    def doPost(json: String): Representation = {
      DetailFiles(derive, parameters, Json.parse(json).asArray)
    }

    lazy val derive = Server.currentConfiguration(
      parameters ++ Map("uri" -> getRequest.getResourceRef.toString),
      getRequest.getChallengeResponse).derive

  }

}
