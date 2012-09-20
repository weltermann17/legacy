package eu.man

package phevos

package mock

/**
 *
 */
import scala.tools.nsc.io.Path.string2path

import com.ibm.haploid.core.config.{getString, getInt, getBoolean}
import com.ibm.haploid.core.newLogger
package object crt {

  import com.ibm.haploid.core.newLogger
  import com.ibm.haploid.core.config._
  
  val embedded_mqbroker_enabled = getBoolean("phevos.crt.jms.use-embedded-broker")
  val embedded_ftpserver_enabled = getBoolean("phevos.crt.ftp.use-embedded-ftpserver")

  val ftp_port = getInt("phevos.crt.ftp.port")
  val ftp_passiveports = getString("phevos.crt.ftp.passive-ports")
  val ftp_user = getString("phevos.crt.ftp.user")
  val ftp_password = getString("phevos.crt.ftp.password")
  val ftp_rootfolder = getString("phevos.crt.ftp.root-dir")

  val mq_broker_url = getString("phevos.crt.jms.url")
  val mq_clientname = getString("phevos.crt.jms.client-name")
  val mq_request_queue = getString("phevos.crt.jms.request-queue")
  val mq_response_queue = getString("phevos.crt.jms.response-queue")

  private[crt] def copyResources(resource: String, destination: String) = {
    val dstfolder = scala.tools.nsc.io.Directory(destination + "/" + resource)

    dstfolder.createDirectory(true, false)

    val files = getFileList(resource)
    files.foreach((name: String) ⇒ {
      com.ibm.haploid.core.util.io.copyBytes(getClass.getResourceAsStream("/" + name), new java.io.FileOutputStream(destination + "/" + name))
    })
  }

  private[this] def getFileList(subfolder: String): List[String] = {
    val jar = new java.util.zip.ZipInputStream(getClass.getProtectionDomain.getCodeSource.getLocation.openStream)
    val buffer = new scala.collection.mutable.ListBuffer[String]

    var entry = jar.getNextEntry

    while (entry != null) {
      if (!entry.isDirectory && entry.getName.startsWith(subfolder)) buffer += entry.getName
      entry = jar.getNextEntry
    }

    buffer.toList
  }
  
  val logger = newLogger(this)

  private[crt] def debug(s: String) = logger.debug(s)
  private[crt] def info(s: String) = logger.info(s)
  private[crt] def warning(s: String) = logger.warning(s)
  private[crt] def error(s: String) = logger.error(s)
  
}

