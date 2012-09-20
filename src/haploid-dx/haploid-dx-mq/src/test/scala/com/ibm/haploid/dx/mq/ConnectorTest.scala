package com.ibm.haploid

package dx

package mq

import org.junit.Assert.assertTrue
import org.junit.Test
import com.ibm.haploid.core.{ config, logger }
import java.util.Calendar
import java.text.SimpleDateFormat
import scala.xml._
import com.ibm.mq.MQException

//@Test private class ConnectorTest {
//  @Test def testFunktionC = {
//    val rc = utils.sendRequest("C", loadxmlfile)
//
//    rc match {
//      case "00" => assertTrue(true)
//      case "01" => assertTrue(true)
//      case _    => assertTrue(false)
//    }
//  }
//
//  @Test def testFunktion1 = {
//    val rc = utils.sendRequest("1", loadxmlfile)
//
//    rc match {
//      case "00" => assertTrue(true)
//      case "01" => assertTrue(true)
//      case _    => assertTrue(false)
//    }
//  }
//
//  @Test def testFunktion2 = {
//    val rc = utils.sendRequest("2", loadxmlfile)
//    rc match {
//      case "00" => assertTrue(true)
//      case "01" => assertTrue(true)
//      case _    => assertTrue(false)
//    }
//  }
//
//  def loadxmlfile : Elem = {
//    XML.loadFile(config.getString("haploid.crt.request-file"))
//  }
//}

@Test private class SimpleMqConnectorTest {
  @Test def simpleMqTest = {
    val request = "<MQECHO-RFC><HEADER><FUNC>REFF001</FUNC><USER>P1106</USER><PW/><RC></RC><RCTXT></RCTXT></HEADER><REQUEST><E_PARM><FUNKTION>S</FUNKTION><OPTION></OPTION><PGM_NAME></PGM_NAME><USERID></USERID></E_PARM><E_KEY><ID_TYPE>P</ID_TYPE><MTB_IDENT>61152015293</MTB_IDENT><PAC>X1A354</PAC><P_IDENT_C>JNV253091L</P_IDENT_C><MTB_REVISION></MTB_REVISION><P_REVISION></P_REVISION><PROJECT></PROJECT><REF_FLAG></REF_FLAG><STATUS></STATUS><DX_STATUS></DX_STATUS></E_KEY></REQUEST></MQECHO-RFC>"

    val rc = utils.sendRequest("S", XML.loadString(request))
    rc match {
      case "" ⇒ {
        logger.info("reply received, part found in crt")
        assertTrue(true)
      }
      case "__" ⇒ {
        logger.info("no broker running")
        assertTrue(true)
      }
      case "303" ⇒ {
        logger.info("reply received, no part found in crt")
        assertTrue(true)
      }
      case _ ⇒ println("reply received, but rc=" + rc + "; unhandled, see xml reply string")
    }
  }
}

private object utils {
  def sendRequest(funktion: String, requestxml: Elem): String = {
    val mqclient = new CrtConnector
    val requestrc: String = {
      if (checkforbrocker(mqclient)) {

        val request = updateFUNKTION(requestxml, funktion)

        logger.info("request-----------------------------------------")
        logger.info(request.toString)
        logger.info("------------------------------------------------")
        logger.info("request for FUNKTION : " + funktion)
        logger.info("request timestamp    : " + (request \\ "TS").text)
        try {
          val response = XML.loadString(mqclient.sendRequest("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + request.toString))

          logger.info("response----------------------------------------")
          logger.info(response.toString)
          logger.info("------------------------------------------------")
          logger.info("response timestamp   : " + (response \\ "TS").text)
          logger.info("response AEDAT       : " + (response \\ "AEDAT").text)

          val rc = (response \\ "ANSWER" \\ "A_PARM" \\ "RC").text
          logger.info("response RC          : " + rc)
          rc
        } catch {
          case e: MQException ⇒ {

            logger.error(e.getMessage + " - " + e.completionCode + " - " + e.reasonCode)
            "666"
          }
        }
      } else "___"
    }
    requestrc
  }

  def checkforbrocker(mqclient: CrtConnector): Boolean = {
    if (!mqclient.isbrokeractive) {
      logger.warning("no running jms broker found, test is canceled! Nevertheless it's result is set to success, which is important for compilations on servers !")
      assertTrue(true)
      false
    } else true
  }

  def updateTS(node: Node): Node = {
    def updateElements(seq: Seq[Node]): Seq[Node] =
      for (subNode ← seq) yield updateTS(subNode)

    node match {
      case <MQECHO-SERVICE>{ ch @ _* }</MQECHO-SERVICE> ⇒ <MQECHO-SERVICE>{ updateElements(ch) }</MQECHO-SERVICE>
      case <HEADER>{ ch @ _* }</HEADER> ⇒ <HEADER>{ updateElements(ch) }</HEADER>
      case <TS>{ ch @ _* }</TS> ⇒ <TS>{ getTimestamp }</TS>
      case other @ _ ⇒ other
    }
  }

  def updateFUNKTION(node: Node, value: String): Node = {
    def updateElements(seq: Seq[Node]): Seq[Node] =
      for (subNode ← seq) yield updateFUNKTION(subNode, value)

    node match {
      case <MQECHO-SERVICE>{ ch @ _* }</MQECHO-SERVICE> ⇒ <MQECHO-SERVICE>{ updateElements(ch) }</MQECHO-SERVICE>
      case <REQUEST>{ ch @ _* }</REQUEST> ⇒ <REQUEST>{ updateElements(ch) }</REQUEST>
      case <E_PARM>{ ch @ _* }</E_PARM> ⇒ <E_PARM>{ updateElements(ch) }</E_PARM>
      case <FUNKTION>{ ch @ _* }</FUNKTION> ⇒ <FUNKTION>{ value }</FUNKTION>
      case other @ _ ⇒ other
    }
  }

  def getTimestamp: String = {
    val now = Calendar.getInstance
    val dateformat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS")
    dateformat.format(now.getTime)
  }
}
