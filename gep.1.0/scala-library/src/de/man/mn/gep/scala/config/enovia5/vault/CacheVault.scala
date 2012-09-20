package de.man.mn.gep.scala.config.enovia5.vault

import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.ByteBuffer

import org.restlet.data.Status
import org.restlet.representation.Representation
import org.restlet.resource.Get
import org.restlet.resource.ServerResource

import com.ibm.de.ebs.plm.scala.concurrent.ops.spawn
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.resource.b
import com.ibm.de.ebs.plm.scala.resource.d
import com.ibm.de.ebs.plm.scala.resource.disposable
import com.ibm.de.ebs.plm.scala.resource.forceContextType
import com.ibm.de.ebs.plm.scala.resource.using
import com.ibm.de.ebs.plm.scala.rest.Files
import com.ibm.de.ebs.plm.scala.rest.Forwarder
import com.ibm.de.ebs.plm.scala.rest.WritableByteChannelRepresentation
import com.ibm.de.ebs.plm.scala.text.StringConversions.fromHexString
import com.ibm.de.ebs.plm.scala.util.Io.addDisposition
import com.ibm.de.ebs.plm.scala.util.Io.buffersize

import de.man.mn.gep.scala.Server

case class CacheVaultResource(
  url: String,
  remotehost: String,
  remoteurl: String,
  parameters: Map[String, String],
  connectionfactory: ConnectionFactory) extends ServerResource {

  val serverfile = fromHexString(parameters("serverfile"))

  object CachedFiles extends Files(url, Server.childContext)

  @Get
  def doRequest = {
    val request = getRequest
    val response = getResponse
    val reference = request.getResourceRef
    reference.setPath(reference.getPath + serverfile)
    CachedFiles.handle(request, response)
    if (404 == response.getStatus.getCode) {
      val forwarder = Forwarder(remotehost + remoteurl, Server.childContext)
      forwarder.handle(request, response)
      response.setStatus(Status.SUCCESS_OK)
      response.setEntity(OutRepresentation(response.getEntity))
    }
    addDisposition(response.getEntity, parameters, "nativeformat")
  }

  private case class OutRepresentation(representation: Representation)
    extends WritableByteChannelRepresentation(representation.getMediaType) {

    setCharacterSet(representation.getCharacterSet)

    override def write(out: java.nio.channels.WritableByteChannel) = {
      var data: FileChannel = null
      try { data = new FileOutputStream(datafile).getChannel } catch { case e => e.printStackTrace }
      try {
        using {
          implicit val _ = forceContextType[Unit]
          val rep = disposable(representation)
          val in = disposable(Channels.newChannel(
            new java.util.zip.GZIPInputStream(rep.getStream, buffersize)))
          val o = disposable(out)
          val buffer = ByteBuffer.allocateDirect(buffersize)
          while (0 <= in.read(buffer)) {
            buffer.flip
            o.write(buffer)
            if (null != data) {
              buffer.rewind
              try { data.write(buffer) } catch { case _ => }
            }
            buffer.clear
          }
          if (null != data) {
            data.close
            implicit val scheduler = Server.getApplication.getTaskService
            spawn { addToCacheVault }
          }
        }
      } catch {
        case e => e.printStackTrace
      }
    }

    private def addToCacheVault: Unit = {
      implicit val scheduler = Server.getApplication.getTaskService
      implicit val logger = Server.logger
      connectionfactory.getConnection(2000) match {
        case Some(connection) =>
          try {
            val statement = connection.createStatement
            val securedfile = new File(new URI(url + serverfile))
            val vault = parameters("vault") + "lv"
            val documentid = parameters("documentid")
            val buf = new StringBuilder
            buf.append("insert into vaultdocument select * from ").append(vault).append(".vaultdocument where oid = hextoraw('").append(documentid).append("')")
            logger.fine(buf.toString)
            statement.executeUpdate(buf.toString)
            buf.setLength(0)
            buf.append("update (select doclocation, description from vaultdocument where oid = hextoraw('").append(documentid).append("')) set doclocation = '").append(securedfile.getAbsolutePath).append("', description = 'GEPserver/1.0'")
            logger.fine(buf.toString)
            statement.executeUpdate(buf.toString)
            connection.commit
            statement.close
            datafile.renameTo(securedfile)
            securedfile.setWritable(false)
            securedfile.setReadable(false, false)
            securedfile.setReadable(true, true)
            logger.info("CacheVault: Successfully created : " + securedfile.getAbsolutePath)
          } catch {
            case e =>
              connection.rollback
              datafile.delete
              logger.severe(e.toString); e.printStackTrace
          } finally {
            connection.close
          }
        case None =>
          datafile.delete
      }
    }
    private lazy val datafile = {
      val file = new File(new URI(url + "/tmp/gep" + serverfile))
      if (!file.getParentFile.exists) file.getParentFile.mkdirs
      file
    }

  }
}
