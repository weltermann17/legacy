package eu.man

package phevos

package dx

package crt

import java.sql.Timestamp

import scala.xml.{ XML, Node }

import com.ibm.haploid.core.{ newLogger, config }
import com.ibm.haploid.dx.mq.CrtConnector

import CrtServices.{ Service2Identifier, Identifier }

trait CrtUtilities {

  val logger = newLogger(this)

  protected def getIndexValue(ident: Identifier, tag: String, function: String): List[String] = {
    getValue(ident, tag, "A_INDEX", function)
  }

  protected def getIdentValue(ident: Identifier, tag: String, function: String): List[String] = {
    getValue(ident, tag, "A_IDENT", function)
  }

  private[this] def getValue(ident: Identifier, tag: String, section: String, function: String): List[String] = {
    val mqclient = new CrtConnector

    val request = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + buildRequestXml(ident, function).toString
    logger.debug("request= " + request)

    val response = XML.loadString(mqclient.sendRequest(request))
    logger.debug("response= " + response)

    val longtext = {
      if ((response \ "ANSWER" \ "A_PARM" \ "RC").text != "") {
        (response \ "ANSWER" \ "A_PARM" \ "FTEXT").text
      } else {
        (response \ "ANSWER" \ section \ tag).text
      }
    }
    List((response \ "ANSWER" \ "A_PARM" \ "RC").text, longtext)
  }

  protected def setService2Values(ident: Service2Identifier): List[String] = {
    val mqclient = new CrtConnector

    val request = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + buildRequestXml_Funktion2(ident).toString
    logger.debug("request= " + request)

    val response = XML.loadString(mqclient.sendRequest(request))
    logger.debug("response= " + response)

    val longtext = (response \ "ANSWER" \ "A_PARM" \ "FTEXT").text

    List((response \ "ANSWER" \ "A_PARM" \ "RC").text, longtext)
  }

  protected def buildRequestXml(ident: Identifier, funktion: String): Node = {
    <MQERFC>
      <HEADER>
        <FUNC>{ config.getString("phevos.dx.crt.request.function") }</FUNC>
        <USER>{ config.getString("phevos.dx.crt.request.user") }</USER>
        <PW>{ config.getString("phevos.dx.crt.request.password") }</PW>
        <RC></RC>
        <RCTEXT></RCTEXT>
      </HEADER>
      <REQUEST>
        <E_PARM>
          <FUNKTION>{ funktion }</FUNKTION>
          <OPTION></OPTION>
          <PGM_NAME>MQS001</PGM_NAME>
          <USERID></USERID>
        </E_PARM>
        <E_KEY>
          <ID_TYPE>P</ID_TYPE>
          <MTB_IDENT>{ ident.shortpartversion }</MTB_IDENT>
          <PAC>X1A354</PAC>
          <P_IDENT_C>{ ident.shortpartnerpartnumber }</P_IDENT_C>
          <MTB_REVISION>{ ident.shortrevision }</MTB_REVISION>
          <P_REVISION>{ getPartnerRevision(ident.partnerrevision) }</P_REVISION>
          <PROJECT></PROJECT>
          <REF_FLAG></REF_FLAG>
          <STATUS></STATUS>
          <DX_STATUS></DX_STATUS>
        </E_KEY>
      </REQUEST>
    </MQERFC>
  }

  private[this] def buildRequestXml_Funktion2(ident: Service2Identifier): Node = {
    <MQERFC>
      <HEADER>
        <FUNC>{ config.getString("phevos.dx.crt.request.function") }</FUNC>
        <USER>{ config.getString("phevos.dx.crt.request.user") }</USER>
        <PW>{ config.getString("phevos.dx.crt.request.password") }</PW>
        <RC></RC>
        <RCTXT></RCTXT>
      </HEADER>
      <REQUEST>
        <E_PARM>
          <FUNKTION>2</FUNKTION>
          <OPTION></OPTION>
          <PGM_NAME>MQS001</PGM_NAME>
          <USERID></USERID>
        </E_PARM>
        <E_KEY>
          <ID_TYPE>P</ID_TYPE>
          <MTB_IDENT>{ ident.shortpartversion }</MTB_IDENT>
          <PAC>X1A354</PAC>
          <P_IDENT_C>{ ident.shortpartnerpartnumber }</P_IDENT_C>
          <MTB_REVISION>{ ident.shortrevision }</MTB_REVISION>
        </E_KEY>
        <E_DATEN>
          <ID_TYPE>P</ID_TYPE>
          <MTB_IDENT>{ ident.shortpartversion }</MTB_IDENT>
          <PAC>X1A354</PAC>
          <P_IDENT_C>{ ident.shortpartnerpartnumber }</P_IDENT_C>
          <MTB_REVISION>{ ident.shortrevision }</MTB_REVISION>
          <P_REVISION>{ ident.partnerrevision }</P_REVISION>
          <P_IDENT></P_IDENT>
          <STATUS></STATUS>
          <REF_FLAG></REF_FLAG>
          <EXPIRE_DATE></EXPIRE_DATE>
          <COLOR_FLAG></COLOR_FLAG>
          <PROJECT></PROJECT>
          <DRAWING_DATE>{ ident.drawingdate }</DRAWING_DATE>
          <DX_STATUS>{ if (ident.dxcompleted) "06" else "" }</DX_STATUS>
          <EZIS_STATUS></EZIS_STATUS>
          <P_TITLE_BLOCK></P_TITLE_BLOCK>
        </E_DATEN>
      </REQUEST>
    </MQERFC>
  }

  private[this] def getTimestamp(date: String) = {
    Timestamp.valueOf(date.substring(6, 10) + "-" + date.substring(3, 5) + "-" + date.substring(0, 2) + " 00:00:00")
  }

  private[this] def getPartnerRevision(rev: Option[String]) = rev match {
    case Some(x) ⇒ x
    case None ⇒ ""
  }

}

