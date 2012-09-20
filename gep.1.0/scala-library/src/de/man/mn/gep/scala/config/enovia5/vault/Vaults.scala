package de.man.mn.gep.scala.config.enovia5.vault

import org.restlet.data._
import org.restlet.routing._
import org.restlet.resource._
import org.restlet.representation._
import com.ibm.de.ebs.plm.scala.rest._
import com.ibm.de.ebs.plm.scala.json._
import com.ibm.de.ebs.plm.scala.text.StringConversions._
import com.ibm.de.ebs.plm.scala.util.Timers._
import com.ibm.de.ebs.plm.scala.database._
import com.ibm.de.ebs.plm.scala.resource._
import de.man.mn.gep.scala.Server
import de.man.mn.gep.scala.Server.ServerType._
import de.man.mn.gep.scala.config.enovia5.catia5._
import com.ibm.de.ebs.plm.scala.rest.Expires
import de.man.mn.gep.scala.config.enovia5.catia5.Detail3dxml
import com.ibm.de.ebs.plm.scala.rest.Expires

class Vaults {

  private def get(name: String) = Server.get(name)

  def apply(
    base: String,
    directory: RepresentationDirectoryCache,
    connectionfactory: ConnectionFactory,
    router: Router) = {

    case class VaultFinder(uritemplate: String) extends Finder(Server.childContext) with HasParameters {

      override def create(request: org.restlet.Request, response: org.restlet.Response, parameters: Map[String, String]): ServerResource = {
        lazy val configuration = Server.currentConfiguration(parameters, request.getChallengeResponse)
        lazy val (host, url) = configuration.hostUrl
        lazy val remotehost = configuration.remoteHost(parameters("division"), parameters("vault"))
        lazy val path = request.getResourceRef.getPath
        lazy val context = Server.childContext
        lazy val isdetail = configuration.is3dxmlPart

        Server.servertype match {
          case Application =>
            RedirectorResource((if (isdetail) remotehost else host) + path, context)
          case Local =>
            val v = if (configuration.sameDivision) {
              LocalVaultResource(Files(url, context), parameters)
            } else {
              CachingForwarderResource(
                directory,
                remotehost + path,
                parameters)
            }
            if (isdetail) Detail3dxml(v, parameters) else v
          case Cache =>
            val v = CacheVaultResource(url, remotehost, path, parameters, connectionfactory)
            if (isdetail) Detail3dxml(v, parameters) else v
          case _ => throw new Exception("Invalid vault configuration: \n" + configuration)
        }
      }
    }

    object Enovia5Vaults extends UriBuilder {
      "/divisions" -> "" -->
        "/{division}/subsystems/enovia5/vaults/{vault}/nativeformats/{nativeformat}/{documentid}/{serverfile}/{nicename}/" --> { _ => s => Expires(VaultFinder(s), 1 year, Server.childContext) } +
        "/{division}/subsystems/enovia5/vaults/{vault}/derivedformats/{derivedformat}/" --> { _ => s => DerivedFormatFinder(s) }
    }

    Enovia5Vaults.attach(router)
  }

}
