package eu.man.phevos

package dx

package crt

import scala.collection.JavaConversions.seqAsJavaList
import scala.xml.XML

import com.ibm.haploid.core.newLogger
import com.ibm.haploid.dx.mq.CrtConnector

import CrtServices.{ TitleBlockFlag, Service2Identifier, PartnerIdentifier, Identifier, CurrentDxStatus, CurrentDrawingDate, CrtInvalidPartStatusException }

object CrtTable extends CrtUtilities {

  def getDxStatus(ident: Identifier): CurrentDxStatus = {

    val buffer = getIndexValue(ident, "DX_STATUS", "S")
    buffer.get(0) match {
      case "" ⇒ CurrentDxStatus(buffer.get(1))
      case _ ⇒ throw new CrtInvalidPartStatusException(buffer.get(1))
    }
  }

  def getDrawingDate(ident: Identifier) = {

    val buffer = getIndexValue(ident, "DRAWING_DATE", "S")

    buffer.get(0) match {
      case "" ⇒ CurrentDrawingDate(buffer.get(1))
      case _ ⇒ throw new CrtInvalidPartStatusException(buffer.get(1))
    }
  }

  def getTitleblock(ident: Identifier) = {
    val buffer = getIndexValue(ident, "P_TITLE_BLOCK", "S")

    buffer.get(0) match {
      case "" ⇒ {
        TitleBlockFlag(buffer.get(1) match {
          case "Y" ⇒ true
          case _ ⇒ false
        })
      }
      case _ ⇒ throw new CrtInvalidPartStatusException(buffer.get(1))
    }
  }

  def getPartnerData(ident: Identifier): PartnerIdentifier = {
    val mqclient = new CrtConnector

    val request = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + buildRequestXml(ident, "S").toString
    logger.debug("request= " + request)
    val responsestring = mqclient.sendRequest(request)

    val response = XML.loadString(responsestring)

    if ((response \ "ANSWER" \ "A_PARM" \ "RC").text == "") {
      val p_ident = (response \ "ANSWER" \ "A_IDENT" \ "P_IDENT").text
      val p_revision = (response \ "ANSWER" \ "A_INDEX" \ "P_REVISION").text
      val drawingdate = (response \ "ANSWER" \ "A_INDEX" \ "DRAWING_DATE").text
      val p_titleblock = if ((response \ "ANSWER" \ "A_INDEX" \ "P_TITLE_BLOCK").text.toLowerCase == "y") true else false
      val p_dxstatus = (response \ "ANSWER" \ "A_INDEX" \ "DX_STATUS").text

      PartnerIdentifier(p_ident, p_revision, drawingdate, p_dxstatus, p_titleblock)

    } else throw new CrtInvalidPartStatusException((response \ "ANSWER" \ "A_PARM" \ "FTEXT").text)
  }

  def setDxStatus(ident: Identifier): CurrentDxStatus = {
    val buffer = getIndexValue(ident, "DX_STATUS", "1")

    buffer.get(0) match {
      case "" ⇒ CurrentDxStatus(buffer.get(1))
      case _ ⇒ throw new CrtInvalidPartStatusException(buffer.get(1))
    }
  }

  def setPartnerDatas(ident: Service2Identifier): Unit = {
    val buffer = setService2Values(ident)

    buffer.get(0) match {
      case "" ⇒ {}
      case _ ⇒ throw new CrtInvalidPartStatusException(buffer.get(1))
    }
  }
}