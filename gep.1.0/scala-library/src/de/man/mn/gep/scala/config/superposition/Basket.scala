package de.man.mn.gep.scala.config.superposition

import scala.collection.immutable.Map

import org.restlet.data.MediaType
import org.restlet.resource.Post
import org.restlet.resource.ServerResource

import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.resource.c
import com.ibm.de.ebs.plm.scala.resource.disposable
import com.ibm.de.ebs.plm.scala.resource.forceContextType
import com.ibm.de.ebs.plm.scala.resource.using
import com.ibm.de.ebs.plm.scala.rest.WritableByteChannelRepresentation
import com.ibm.de.ebs.plm.scala.util.Io.addDisposition

import de.man.mn.gep.scala.config.enovia5.catia5.Derive
import de.man.mn.gep.scala.Server

class Basket(derive: Derive, entries: Json.JArray)
  extends WritableByteChannelRepresentation(MediaType.APPLICATION_ZIP) {

  override def write(out: java.io.OutputStream) = {
    implicit val _ = forceContextType[Unit]
    using {
      derive.out = out
      derive.entries = entries
      val zipout = disposable(derive.zipout)
      derive.writeManifest
      derive.writeBasketEntries
      derive.writeBasketAssembly
      derive.writeBasketConfig
    }
  }
}

object Basket {

  def apply(parameters: Map[String, String]) = {
    new ServerResource {
      @Post
      def doPost(json: String) = {
        val basket = new Basket(derive, Json.parse(json).asArray)
        getResponse.setEntity(basket)
        addDisposition(basket, parameters, "derivedformat")
      }

      lazy val derive = Server.currentConfiguration(
        parameters ++ Map("uri" -> getRequest.getResourceRef.toString),
        getRequest.getChallengeResponse).derive
    }
  }

}
