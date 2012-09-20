package de.man.mn.gep.scala.config

import org.restlet.Restlet
import org.restlet.routing.Router
import com.ibm.de.ebs.plm.scala.rest._
import com.ibm.de.ebs.plm.scala.database._
import de.man.mn.gep.scala.config.user._
import de.man.mn.gep.scala.Server
import de.man.mn.gep.scala.Server.ServerType._
import com.ibm.de.ebs.plm.scala.rest.BasicAuthenticator

case class Plm(
  baseuri: String,
  directory: RepresentationDirectoryCache,
  connectionfactory: ConnectionFactory) extends Restlet {

  lazy val context = Server.childContext

  lazy val metadataclient = new enovia5.metadata.MetadataClient
  lazy val metadataserver = new enovia5.metadata.MetadataServer
  lazy val vaults = new enovia5.vault.Vaults
  lazy val superpos = new superposition.Superposition

  Server.servertype match {
    case Application =>
      metadataclient.apply(directory, router)
      vaults.apply(baseuri, directory, connectionfactory, router)
      superpos.apply(router)
    case Enovia5Database =>
      metadataserver.apply(baseuri, connectionfactory, router)
    case Local | Cache =>
      vaults.apply(baseuri, directory, connectionfactory, router)
  }

  def apply = BasicAuthenticator(router, UserVerifier, context) ++ "/nativeformats/jpg/" ++ "/nativeformats/png/"

  private lazy val router = new Router

}