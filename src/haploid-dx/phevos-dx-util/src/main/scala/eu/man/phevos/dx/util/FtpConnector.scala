package eu.man.phevos.dx.util

import org.apache.commons.net.ftp._
import com.ibm.haploid.core.{ config, logger }
import com.ibm.haploid.core.util.io._

import scala.xml._

class FtpConnector {

  /**
   * Returns a String with the content of a given file loaded per FTP form
   * the FTP server defined by the application.conf.
   *
   * @param		name 	the name of the file to download
   * @return			the string with the content of the file
   *
   */
  def getFileAsString(name: String): String = {
    val out = new java.io.ByteArrayOutputStream

    try {
      open
      ftp.retrieveFile(name, out)

      new String(out.toByteArray(), "UTF-8")

    } catch {
      case e: Exception ⇒ {
        e.getLocalizedMessage

      }
    } finally {
      if (out != null) out.close
      close
    }
  }

  /**
   * Returns a scala.xml.Elem with the content of a given file loaded per FTP form
   * the FTP server defined by the application.conf.
   *
   * @param		name 	the name of the file to download
   * @return			the XML element with the content of the file
   *
   */
  def getFileAsXML(name: String): xml.Elem = {
    val out = new java.io.ByteArrayOutputStream

    try {
      open
      ftp.retrieveFile(name, out)
      XML.loadString(new String(out.toByteArray(), "UTF-8"))
    } finally {
      if (out != null) out.close
      close
    }
  }

  /**
   * Returns a list of file names from the FTP server which fulfill the requirement
   * to have the same prefix as defined in the application.conf.
   * The prefix is case sensitive.
   *
   * @return			the List[String] with the found file names
   *
   */
  def getFileList(): List[String] = {
    getFileList(new FtpFilter(config.getString("phevos.dx.util.ftp.prefix")))
  }

  /**
   * Returns a list of file names from the FTP server which fulfill the requirement
   * to have the same prefix as defined with the prefix parameter.
   * The prefix is case sensitive.
   *
   * @param		prefix	the string which defines the prefix for files which are included
   * @return			the List[String] with the found file names
   *
   */
  def getFileList(prefix: String): List[String] = {
    getFileList(new FtpFilter(prefix))
  }

  /**
   * Returns the name of the most recent file from the FTP server with the same prefix as defined by
   * the prefix parameter. The decision is done by the file name, so it's expected that a time
   * stamp with the format YYMMDD-hhmmss is part of the file name. The time stamp of the file is
   * not took in account.
   * The prefix is case sensitive.
   *
   * @param		prefix	the string which defines the prefix for files which are included
   * @return			the string with the file name
   *
   */
  def getRecentFile(prefix: String): String = {
    val files = getFileList(prefix)
    if (files.length > 0) {
      val sorted = files sortWith (_ > _)
      sorted.head
    } else {
      ""
    }
  }

  /**
   * Returns the name of the most recent file from the FTP server with the same prefix as defined in
   * the application.conf. The decision is done by the file name, so it's expected that a time stamp
   * with the format YYMMDD-hhmmss is part of the file name. The time stamp of the file is not took
   * in account.
   * The prefix is case sensitive.
   *
   * @return			the string with the file name
   *
   */
  def getRecentFile(): String = {
    val files = getFileList()
    if (files.length > 0) {
      val sorted = files sortWith (_ > _)
      sorted.head
    } else {
      ""
    }
  }

  /**
   * Checks, if the internal FTP client is connected to the server.
   *
   * @return			true, if a connection is open, else false
   */
  def isConnected: Boolean = ftp.isConnected()

  private def getFileList(filter: FtpFilter): List[String] = {
    open
    val result = if (ftp.isConnected) {
      val files = ftp.listFiles("", filter)
      files.toList collect { case file: FTPFile ⇒ file.getName }
    } else {
      List("")
    }
    close
    result
  }

  private def open(): Unit = {
    open(ftp_server, ftp_port, ftp_user, ftp_password)
  }

  private def open(url: String, port: Int, user: String, password: String) = {
    if (ftp != null) {
      if (!ftp.isConnected) {

        ftp.connect(url, port)
        ftp.login(user, password)
        ftp.enterLocalPassiveMode

        logger.info("FTP connection to " + url + " established")
      }
    } else {

      ftp.connect(url)
      ftp.login(user, password)
      ftp.enterLocalPassiveMode

      logger.info("FTP connection to " + url + " established")
    }

  }

  private def close = {
    if (ftp != null) {
      if (ftp.isConnected) {
        ftp.logout
        ftp.disconnect
        logger.info("FTP connection closed")
      }
    }
  }

  var ftp: FTPClient = new FTPClient
}

class FtpFilter(prefix: String) extends FTPFileFilter {

  def accept(file: FTPFile): Boolean = {
    if (file.getName.toUpperCase.startsWith(prefix)) true else false
  }

}