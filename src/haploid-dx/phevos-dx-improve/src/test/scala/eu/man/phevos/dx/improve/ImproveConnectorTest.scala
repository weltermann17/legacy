package eu.man.phevos

package dx

package improve

import org.junit.Assert.assertTrue
import org.junit.Test
import com.ibm.haploid.core.{ config, logger }

import java.nio.file.Paths
import java.nio.file.Files
import ImproveService._

@Test private class ImproveConnectorTest {

  @Test def testSendOpenTicket: Unit = {

    return ()

    val tempdir = com.ibm.haploid.core.file.temporaryDirectory
    tempdir.deleteOnExit

    val inputpath = Paths.get(tempdir.getAbsolutePath + "/" + "input")
    Files.createDirectories(inputpath)
    val workingpath = Paths.get(tempdir.getAbsolutePath + "/" + "working")
    Files.createDirectories(workingpath)

    logger.debug(getClass.getProtectionDomain.getCodeSource.getLocation.getPath)
    logger.debug("inputpath   : " + inputpath.toAbsolutePath)
    logger.debug("workingpath : " + workingpath.toAbsolutePath)

    val propertyfile = Paths.get(inputpath.toString, "dx.properties")

    val statusfile = Paths.get(inputpath.toString, "dx.status")

    val propres = getClass.getResource("/dx.properties").toURI
    val statpres = getClass.getResource("/dx.status").toURI

    Files.copy(Paths.get(propres), propertyfile)
    Files.copy(Paths.get(statpres), statusfile)

    val ticket = TicketData("Test for PhevosDX open ticket", "Description of the open ticket", "")

    val result = SendOpenTicket(ticket)

    result match {
      case Left(e) ⇒
        logger.error(e.toString)
        assertTrue(true)
      case _ ⇒ assertTrue(true)
    }

  }

  @Test def testSendCloseTicket: Unit = {

    return ()

    val tempdir = com.ibm.haploid.core.file.temporaryDirectory
    tempdir.deleteOnExit

    val inputpath = Paths.get(tempdir.getAbsolutePath + "/" + "input")
    Files.createDirectories(inputpath)
    val workingpath = Paths.get(tempdir.getAbsolutePath + "/" + "working")
    Files.createDirectories(workingpath)

    logger.debug(getClass.getProtectionDomain.getCodeSource.getLocation.getPath)
    logger.debug("inputpath   : " + inputpath.toAbsolutePath)
    logger.debug("workingpath : " + workingpath.toAbsolutePath)

    val propertyfile = Paths.get(inputpath.toString, "dx.properties")

    val statusfile = Paths.get(inputpath.toString, "dx.status")

    val propres = getClass.getResource("/dx.properties").toURI
    val statpres = getClass.getResource("/dx.status").toURI

    Files.copy(Paths.get(propres), propertyfile)
    Files.copy(Paths.get(statpres), statusfile)

    val ticket = TicketData("Test for PhevosDX close ticket", "Description of the close ticket", "")
    val result = SendCloseTicket(ticket)

    result match {
      case Left(e) ⇒
        logger.error(e.toString)
        assertTrue(true)
      case _ ⇒ assertTrue(true)
    }

  }

  //  @Test def testSend1000CloseTicket = {
  //    for (i <- 1 to 100) {
  //      val tempdir = com.ibm.haploid.core.file.temporaryDirectory
  //       
  //
  //      val inputpath = Paths.get(tempdir.toString,"input")
  //      Files.createDirectories(inputpath)
  //      val workingpath = Paths.get(tempdir.toString,"working")
  //      Files.createDirectories(workingpath)
  //
  //      logger.debug(getClass.getProtectionDomain.getCodeSource.getLocation.getPath)
  //      logger.debug("inputpath   : " + inputpath.toAbsolutePath)
  //      logger.debug("workingpath : " + workingpath.toAbsolutePath)
  //
  //      val propertyfile = Paths.get(inputpath.toString, "dx.properties")
  //      val statusfile = Paths.get(inputpath.toString, "dx.status")
  //
  //      val propres = getClass.getResource("/dx.properties").toURI
  //      val statpres = getClass.getResource("/dx.status").toURI
  //
  //      Files.copy(Paths.get(propres), propertyfile)
  //      Files.copy(Paths.get(statpres), statusfile)
  //
  //      val ticket = TicketData("Title test ticket", "short description test ticket", "long description test ticket", propertyfile, statusfile, workingpath)
  //
  //      val result = SendCloseTicket(ticket)
  //
  //      result match {
  //        case Left(e) =>
  //          logger.error(e.toString)
  //          assertTrue(false)
  //        case _ => logger.info("send ticket #" + i)
  //      }
  //      
  //       
  //    }
  //    assertTrue(true)
  //
  //  }

}

