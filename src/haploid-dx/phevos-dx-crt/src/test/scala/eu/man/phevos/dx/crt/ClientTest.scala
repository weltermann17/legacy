package eu.man.phevos

package dx

package crt

import org.junit.Assert.assertTrue
import org.junit.Test

import java.util.Calendar
import java.text.SimpleDateFormat
import scala.xml._

import com.ibm.haploid.core.{ config, logger }
import com.ibm.haploid.dx.mq.CrtConnector

import crt.CrtServices._
import util.interfaces.PartInfo

@Test private class ServiceTests {
  @Test def testGetPartList = {

    val result = EntitledDxStatus({})

    logger.info("--------- parts ---------")

    result match {
      case Right(partlist) ⇒ {
        partlist.partinfos.foreach((part: PartInfo) ⇒ {
          logger.info("PartInfo: " + part.mtbPartNumber + "; " + part.mtbPartIndex + "; " + part.vwPartNumber + "; " + part.vwKStand + "; " + part.dxStatus + "; " + part.vwDrawingDate)
        })
        logger.info("------------------------")
      }
      case Left(e: CrtInvalidPartStatusException) ⇒
        logger.error(e.toString)
        assertTrue(true)
      case Left(e: Exception) ⇒
        logger.error(e.toString)
        assertTrue(false)
    }

  }

  @Test def testReceiveDxStatus = {

    val ident = new Identifier("65.99192-0000", "   ", "23B.900.031", Option(""))
    val result = DxStatus(ident)

    result match {
      case Right(dxstatus) ⇒ {
        logger.info("DX_STATUS : " + dxstatus)
      }
      case Left(e: CrtInvalidPartStatusException) ⇒
        logger.error(e.toString)
        assertTrue(true)
      case Left(e: Exception) ⇒
        logger.error(e.toString)
        assertTrue(false)
    }
  }

  @Test def testGetPartData = {
    val ident = new Identifier("06.01013-7127", "", "WHT.006.029", Option("K2"))
    val result = PartnerIdent(ident)

    result match {
      case Right(value) ⇒ {
        logger.info("partner ident= " + value.partnerpartnumber)
        if (value.partnerrevision == "") logger.info("no partner revision") else logger.info("partner revision= " + value.partnerrevision)
        if (value.drawingdate == "") logger.info("no drawing date") else logger.info("drawing date= " + value.drawingdate)
        logger.info("dx status= " + value.dxstatus)
        logger.info("has titleblock= " + value.titleblockexists)
      }
      case Left(e: CrtInvalidPartStatusException) ⇒
        logger.error(e.toString)
        assertTrue(true)
      case Left(e: Exception) ⇒
        logger.error(e.toString)
        assertTrue(false)
    }

  }

  @Test def testReceiveValidSheets = {
    val ident = new Identifier("81.25902-8116", "BF_", "", Option(""))
    val result = ValidSheets(ident)

    result match {
      case Right(sheets) ⇒ {
        logger.info("sheets= " + sheets)
        assertTrue(true)
      }
      case Left(e: CrtInvalidPartStatusException) ⇒
        logger.error(e.toString)
        assertTrue(true)
      case Left(e: Exception) ⇒
        logger.error(e.toString)
        assertTrue(false)
    }
  }

  @Test def testReceiveDrawingDate = {

    val ident = new Identifier("61.15201-5293", "", "JNV.253.091.L", Option(""))
    val result = DrawingDate(ident)
    result match {
      case Right(drawingdate) ⇒ {
        if (drawingdate.date == "") logger.info("empty drawing date") else logger.info("VW drawing date= " + drawingdate.date)
        assertTrue(true)
      }
      case Left(e: CrtInvalidPartStatusException) ⇒
        logger.error(e.toString)
        assertTrue(true)
      case Left(e: Exception) ⇒
        logger.error(e.toString)
        assertTrue(false)
    }
  }

  @Test def testReceiveTitleblock = {

    val ident = new Identifier("61.15201-5293", "", "JNV.253.091.L", Option(""))
    val result = TitleBlock(ident)

    result match {
      case Right(hastitleblock) ⇒ {
        logger.info("has VW titleblock= " + hastitleblock.valid)
        assertTrue(true)
      }
      case Left(e: CrtInvalidPartStatusException) ⇒
        logger.error(e.toString)
        assertTrue(true)
      case Left(e: Exception) ⇒
        logger.error(e.toString)
        assertTrue(false)
    }
  }

  @Test def testSetDxStatus = {
    val ident = new Identifier("06.01013-7127", "", "WHT.006.029", Option("K2"))

    val result = SetDxStatusToWork(ident)

    result match {
      case Right(result) ⇒ {
        logger.info("result= " + result.status)
        assertTrue(true)
      }
      case Left(e: CrtInvalidPartStatusException) ⇒
        logger.error(e.toString)
        assertTrue(true)
      case Left(e: Exception) ⇒
        logger.error(e.toString)
        assertTrue(false)
    }
  }

  @Test def testSetServiceS2 = {

    //    val ident = new Service2Identifier("65.99192-0000", "   ", "23B.900.031",false,"","28.06.2012")
    val ident = new Service2Identifier("06.01013-7127", "   ", "WHT.006.029", false, "", "03.07.2012")
    val result = SetKstandDrawingDateDxStatus(ident)

    result match {
      case Right(result) ⇒ {
        logger.info("result= " + result)
        assertTrue(true)
      }
      case Left(e: CrtInvalidPartStatusException) ⇒
        logger.error(e.toString)
        assertTrue(true)
      case Left(e: Exception) ⇒
        logger.error(e.toString)
        assertTrue(false)
    }
  }
}

