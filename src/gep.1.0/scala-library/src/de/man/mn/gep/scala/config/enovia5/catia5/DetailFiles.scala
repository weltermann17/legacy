package de.man.mn.gep.scala.config.enovia5.catia5

import java.util.zip.Deflater
import java.util.zip.ZipOutputStream

import scala.collection.immutable.Map

import org.restlet.data.MediaType
import org.restlet.data.Method
import org.restlet.Request
import org.restlet.Response

import com.ibm.de.ebs.plm.scala.json.JsonConversions.Any2Json
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.resource.c
import com.ibm.de.ebs.plm.scala.resource.disposable
import com.ibm.de.ebs.plm.scala.resource.forceContextType
import com.ibm.de.ebs.plm.scala.resource.using
import com.ibm.de.ebs.plm.scala.rest.Files
import com.ibm.de.ebs.plm.scala.rest.WritableByteChannelRepresentation
import com.ibm.de.ebs.plm.scala.util.Io.addZipEntry

import de.man.mn.gep.scala.Server

case class DetailFiles(
  derive: Derive,
  parameters: Map[String, String],
  localfiles: Json.JArray)
  extends WritableByteChannelRepresentation(MediaType.APPLICATION_ZIP) {

  override def write(out: java.io.OutputStream) = {
    using {
      implicit val _ = forceContextType[Unit]
      implicit val zipout = disposable(new ZipOutputStream(out))
      val files = Files(url, Server.childContext)
      zipout.setLevel(if (configuration.isRemote) Deflater.DEFAULT_COMPRESSION else Deflater.NO_COMPRESSION)
      localfiles.toList.foreach { e =>
        val filename = e.asObject.get("filename").asString
        val filepath = e.asObject.get("filepath").asString
        val localrequest = new Request(Method.GET, filepath)
        val localresponse = new Response(localrequest)
        files.handle(localrequest, localresponse)
        if (localresponse.getStatus.isSuccess) {
          addZipEntry(filename) {
            localresponse.getEntity.write(zipout)
          }
        } else {
          Server.logger.severe(localresponse.getStatus.toString)
        }
      }
    }
  }

  private lazy val configuration = Server.currentConfiguration(parameters, null)
  private lazy val (host, url) = configuration.hostUrl

}
