package eu.man.phevos.mock

package improve

import com.ibm.haploid.rest.HaploidRestServer
import com.ibm.haploid.rest.HaploidService

import com.ibm.haploid.core.{ config, logger }

import org.apache.ftpserver._
import org.apache.ftpserver.listener._
import org.apache.ftpserver.usermanager._
import org.apache.ftpserver.usermanager.impl._
import org.apache.ftpserver.ftplet._

import java.util.ArrayList
import java.io._

import scala.actors._
import scala.tools.nsc.io._

object Main extends App with HaploidRestServer {

  override def onTermination = {
    FtpServer ! Stop

    0
  }

  FtpServer.start

}

object FtpServer extends Actor {
  def act() {
    val srvfact = new FtpServerFactory
    val usrmgrfact = new PropertiesUserManagerFactory
    val conconffact = new ConnectionConfigFactory

    conconffact.setAnonymousLoginEnabled(true)
    val conconf = conconffact.createConnectionConfig
    srvfact.setConnectionConfig(conconf)

    val folder = Directory((new java.io.File(getClass.getProtectionDomain.getCodeSource.getLocation.getPath).getParentFile.getPath + ftp_rootfolder))

    logger.debug("ftp root folder : " + folder)
    logger.debug("ftp work folder : " + ftp_workingfolder)

    if (folder.exists == false) {
      folder.createDirectory(true, false)
      logger.debug("created FTP root folder : " + folder.toAbsolute.path)
    }

    if (ftp_workingfolder != "") {
      val workfolder = folder + ftp_workingfolder
      if (!Directory(workfolder).exists) {
        Directory(workfolder).createDirectory(true, false)
        logger.debug("created FTP work folder : " + workfolder)
      }
    }

    val authorities: ArrayList[org.apache.ftpserver.ftplet.Authority] = new ArrayList()
    authorities.add(new WritePermission)

    val user = new BaseUser
    user.setName(ftp_user)
    user.setPassword(ftp_password)
    user.setHomeDirectory(folder.toString)
    user.setAuthorities(authorities)

    val usrmgr = usrmgrfact.createUserManager
    usrmgr.save(user)
    srvfact.setUserManager(usrmgr)

    val listfact = new ListenerFactory
    if (ftp_port != 0) listfact.setPort(ftp_port)

    val dataconconffact = new DataConnectionConfigurationFactory
    dataconconffact.setPassivePorts(ftp_passiveports)

    listfact.setDataConnectionConfiguration(dataconconffact.createDataConnectionConfiguration)

    srvfact.addListener("default", listfact.createListener)
    val ftpserver = srvfact.createServer

    ftpserver.start
    logger.info("Improve Mockup FTP server started")

    receive {
      case Stop ⇒
        {
          logger.info("stopping Improve Mockup FTP server")
        }
        ftpserver.stop
    }
  }
}

case object Stop

class ImproveService extends HaploidService {

  lazy val service = path("Improve") {
    completeWith("I'm Improve")
  }
}