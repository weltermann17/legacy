package eu.man

package phevos

package dx

package improve

import java.io.ByteArrayInputStream
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.{ Scanner, Date }

import org.apache.commons.net.ftp.{ FTPClient, FTP }
import org.apache.commons.net.util.Base64

import com.ibm.haploid.core.{ newLogger, config }
import com.ibm.haploid.core.util.Uuid

import eu.man.phevos.dx.improve.ImproveService.TicketData

trait ImproveConnector {

  val dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
  val priority = ""
  val status = ""
  val solver = ""

  val ftp_port = config.getInt("phevos.dx.improve.ftp.port")
  val ftp_user = config.getString("phevos.dx.improve.ftp.user")
  val ftp_password = config.getString("phevos.dx.improve.ftp.password")
  val ftp_workingfolder = config.getString("phevos.dx.improve.ftp.working-folder")
  val ftp_server = config.getString("phevos.dx.improve.ftp.server")

  val site = config.getString("phevos.dx.improve.ticket.site")
  val project = config.getString("phevos.dx.improve.ticket.project")
  val category = config.getString("phevos.dx.improve.ticket.category")

  val openpriority = config.getString("phevos.dx.improve.ticket.open.priority")
  val closepriority = config.getString("phevos.dx.improve.ticket.close.priority")

  val errcodeprefix = config.getString("phevos.dx.improve.ticket.error-code-prefix")

  def apply(ticket: TicketData) = {

    val errorcode = errcodeprefix + Uuid.uuid2string(Uuid.newUuid)

    val xmlstring = createTicketXML(errorcode, category, priority, ticket.title, ticket.description, solver, status, ticket.dxProperties)

    newLogger(this).debug(xmlstring)

    sendTicketXMLtoServer(xmlstring, errorcode)
  }

  private[this] def createTicketXML(errorcode: String,
    category: String,
    priority: String,
    title: String,
    description: String,
    solver: String,
    status: String,
    dxProperties: String) = {

    val ticketXml = new StringBuilder

    ticketXml.append("<ticket>");

    ticketXml.append("<error_code>");
    ticketXml.append(errorcode);
    ticketXml.append("</error_code>");

    ticketXml.append("<once_only>");
    ticketXml.append(1);
    ticketXml.append("</once_only>");

    ticketXml.append("<projekt>");
    ticketXml.append(project);
    ticketXml.append("</projekt>");

    ticketXml.append("<art>");
    ticketXml.append(category);
    ticketXml.append("</art>");

    //    ticketXml.append("<user_id>");
    //    ticketXml.append(solver);
    //    ticketXml.append("</user_id>");

    ticketXml.append("<solver_id>");
    ticketXml.append(solver);
    ticketXml.append("</solver_id>");

    ticketXml.append("<status>");
    ticketXml.append(status);
    ticketXml.append("</status>");

    ticketXml.append("<dringlichkeit>");
    ticketXml.append(priority);
    ticketXml.append("</dringlichkeit>");

    ticketXml.append("<kurzbeschreibung><![CDATA[");
    ticketXml.append(title);
    ticketXml.append("]]></kurzbeschreibung>");

    ticketXml.append("<beschreibung><![CDATA[");
    ticketXml.append(description);
    ticketXml.append("]]></beschreibung>");

    ticketXml.append("<standort>");
    ticketXml.append(site);
    ticketXml.append("</standort>");

    ticketXml.append("<attachments>");
    ticketXml.append("<file name=\"unload.xml\" description=\"unload.xml\">");
    ticketXml.append("<![CDATA[");
    ticketXml.append(encodeAttachment(dxProperties));
    ticketXml.append("]]>");
    ticketXml.append("</file>");
    ticketXml.append("</attachments>");

    //    val propertytxt = Paths.get(workingfolder.toString, propertyfile.getFileName + ".txt")
    //    val statustxt = Paths.get(workingfolder.toString, statusfile.getFileName + ".txt")
    //
    //    java.nio.file.Files.copy(propertyfile, propertytxt)
    //    java.nio.file.Files.copy(statusfile, statustxt)
    //
    //    ticketXml.append("<attachments>");
    //    ticketXml.append("<file name=\"" + propertytxt.getFileName + "\" description=\"properties file for " + "\">");
    //    ticketXml.append("<![CDATA[");
    //    ticketXml.append(encodeAttachment(propertytxt));
    //    ticketXml.append("]]>");
    //    ticketXml.append("</file>");
    //
    //
    //    ticketXml.append("<file name=\"" + statustxt.getFileName + "\" description=\"status file for " + title + "\">");
    //    ticketXml.append("<![CDATA[");
    //    ticketXml.append(encodeAttachment(statustxt));
    //    ticketXml.append("]]>");
    //    ticketXml.append("</file>");
    //    ticketXml.append("</attachments>");
    ticketXml.append("</ticket>");

    ticketXml.toString
  }
  private[this] def sendTicketXMLtoServer(xmlstring: String, errorcode: String) = {
    try {
      val ftp = new FTPClient

      if (ftp_port == 0) ftp.connect(ftp_server) else ftp.connect(ftp_server, ftp_port)
      ftp.login(ftp_user, ftp_password)

      ftp.enterLocalPassiveMode();
      ftp.setFileType(FTP.ASCII_FILE_TYPE);

      val input = new ByteArrayInputStream(xmlstring.getBytes);
      if (ftp_workingfolder != "") ftp.changeWorkingDirectory(ftp_workingfolder);
      val filename = ("ticket_" + errorcode + "_" + dateFormat.format(new Date()) + ".xml").toLowerCase

      ftp.storeFile(filename, input)
      input.close();

      ftp.logout();
      ftp.disconnect();
    } catch {
      case e: Throwable ⇒
        val ex = new Exception(e.getMessage() + " with " + ftp_server + ":" + ftp_port + " (" + ftp_user + ", " + ftp_password + ")")
        ex.setStackTrace(e.getStackTrace)
        throw ex
    }
  }

  /**
   * Read the file and encode it with Base64
   *
   * @param filepath
   *            Path and filename
   * @return Encoded file data
   * @throws FileNotFoundException
   */

  private[this] def encodeAttachment(file: Path): String = {

    val scanner = new Scanner(file);
    val fileData = new StringBuilder();

    try {
      while (scanner.hasNextLine()) {
        fileData.append(scanner.nextLine() + "\n");
      }
    } finally {
      scanner.close();
    }

    encodeAttachment(fileData.toString())
  }

  private[this] def encodeAttachment(s: String): String = {
    val base64encoder = new Base64
    new String(base64encoder.encode(s.getBytes()))
  }
}

object OpenTicket extends ImproveConnector {
  override val priority = "3"
  override val status = "1"

}

object CloseTicket extends ImproveConnector {
  override val priority = "3"
  override val status = "9"
  override val solver = config.getString("phevos.dx.improve.ticket.close.solver")
}

//  def sendOpenTicket(ticket : TicketData) : Unit = {
//
//    //    val xmlstring = createTicketXML(ticket.title,
//    //      "1",
//    //      openpriority,
//    //      ticket.shortdescription,
//    //      ticket.description,
//    //      "")
//
//  }
//
//  def sendCloseTicket(ticket : TicketData) : Unit = {
//
//    //      def xmlstring = createTicketXML(ticket.title,
//    //        "9",
//    //        closepriority,
//    //        ticket.shortdescription,
//    //        ticket.description,
//    //        solver)
//
//    //    logger.debug("close ticket xml")
//    //    logger.debug(xmlstring)
//    //
//    //    sendTicketXMLtoServer(xmlstring, ticket.title, "9")
//
//  }

