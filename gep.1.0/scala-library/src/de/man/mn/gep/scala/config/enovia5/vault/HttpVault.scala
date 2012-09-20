package de.man.mn.gep.scala.config.enovia5.vault

import java.io._
import java.nio._
import java.nio.channels._
import com.ibm.de.ebs.plm.scala.json._
import com.ibm.de.ebs.plm.scala.json.JsonConversions._
import com.ibm.de.ebs.plm.scala.rest._
import com.ibm.de.ebs.plm.scala.rest.Redirections._
import com.ibm.de.ebs.plm.scala.text.StringConversions._
import com.ibm.de.ebs.plm.scala.util.Timers._
import com.ibm.de.ebs.plm.scala.util.Io._
import com.ibm.de.ebs.plm.scala.resource._
import org.restlet._
import org.restlet.data._
import org.restlet.engine.application._
import org.restlet.representation._
import org.restlet.resource._
import org.restlet.routing._
import org.restlet.util._
import de.man.mn.gep.scala.config.user._
import de.man.mn.gep.scala.Server
import de.man.mn.gep.scala.Configuration
import com.ibm.de.ebs.plm.scala.rest.Expires
import com.ibm.de.ebs.plm.scala.rest.BasicAuthenticator

class HttpVault {

  val context = Server.childContext
  val secured = "/secured0"

  Configuration.getConfig.foreach {
    case ((division, location, vaulttype), (host, url)) =>
      if (Configuration.thisDivision == division && Configuration.thisLocation == location && Configuration.thisType == vaulttype) {
        Server.attach(
          url.substring(6) + secured + "/",
          Expires(
            Files(
              url + secured,
              context),
            1 day,
            context))
      } else if (Configuration.thisDivision == division && Configuration.thisLocation != location && "local" == vaulttype) {
        router.attach(
          url.substring(11) + "/{folder}/{file}",
          Expires(
            EncodingRedirector(division, host, location, context),
            1 day,
            context))
      }
  }

  private case class EncodingRedirector(division: String, host: String, location: String, context: Context)
    extends Filter {

    val nicename = toHexString("none")
    val nativeformat = "txt"
    val documentid = toHexString("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
    val relative = "/plm/divisions/" + division + "/subsystems/enovia5/vaults/" + location +
      "/nativeformats/" + nativeformat + "/" + documentid + "/{serverfile}/" + nicename + "/"

    setNext(Forwarder(host + relative, context))

    override def beforeHandle(request: Request, response: Response) = {
      val serverfile = toHexString("/" + request.getAttributes.get("folder").toString + "/" + request.getAttributes.get("file").toString)
      request.getAttributes.put("serverfile", serverfile)
      forceGzip(request)
      Filter.CONTINUE
    }

    override def afterHandle(request: Request, response: Response) = {
      if (response.getStatus.isSuccess) {
        response.setEntity(DecodingRepresentation(response.getEntity))
      }
    }

    case class DecodingRepresentation(representation: Representation)
      extends WritableByteChannelRepresentation(representation.getMediaType) {

      setCharacterSet(representation.getCharacterSet)

      override def write(out: java.nio.channels.WritableByteChannel) = {
        using {
          implicit val _ = forceContextType[Unit]
          val rep = disposable(representation)
          val in = disposable(Channels.newChannel(detectStream))
          val o = disposable(out)
          val buffer = ByteBuffer.allocateDirect(buffersize)
          while (0 <= in.read(buffer)) {
            buffer.flip
            o.write(buffer)
            buffer.clear
          }
        }
      }

      private lazy val detectStream = {
        val in = new BufferedInputStream(Channels.newInputStream(representation.getChannel), buffersize)
        try {
          in.mark(2)
          new java.util.zip.GZIPInputStream(in, buffersize)
        } catch {
          case _ => in.reset; in
        }
      }
    }
  }

  def apply = BasicAuthenticator(router, UserVerifier, context)

  private lazy val router = new Router(context)
}
