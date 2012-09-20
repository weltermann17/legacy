package eu.man

package phevos

package dx

package crt

import scala.collection.mutable.ListBuffer
import scala.xml.{ XML, Node }

import com.ibm.haploid.core.config
import com.ibm.haploid.dx.mq.CrtConnector

import CrtServices.{ Sheets, Identifier, CrtInvalidPartStatusException }
import util.interfaces.{ ReleaseLevel, EZISSheetMetadata }

object SadisTable extends CrtUtilities {

  def getValidSheets(ident: Identifier): Sheets = {

    val mqclient = new CrtConnector

    val request = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + buildSadisRequestXml(ident).toString

    val response = XML.loadString(mqclient.sendRequest(request))

    logger.debug("response= " + response)

    if ((response \ "UERROR").text != "") {
      throw new Exception("Error while interacting with SADIS, RC=" + (response \ "UERROR" \ "RC").text + ", RCTEXT=" + (response \ "UERROR" \ "RCTXT").text)
    }

    if ((response \ "HEADER" \ "RC").text != "") {
      throw new CrtInvalidPartStatusException((response \ "HEADER" \ "RCTXT").text)
    } else {

      def nodeNameEqual(label: String)(node: Node) = {
        (node.label.equals(label))
      }

      val nodes = (response \ "REPLY" \\ "_" filter nodeNameEqual("RT_WERTE"))

      val result: ListBuffer[EZISSheetMetadata] = ListBuffer()
      nodes.foreach(node ⇒ {
        val rl = (if ((node \ "VORAB").text.toLowerCase == "n") ReleaseLevel.KN else ReleaseLevel.E)

        result += new EZISSheetMetadata((node \ "SNR_AUFB").text, (node \ "BLATT").text.toInt, (node \ "AEZU_DOK").text.trim, rl)
      })

      Sheets(result.toList)
    }

  }

  protected def buildSadisRequestXml(ident: Identifier): Node = {

    <MQECHO-RFC>
      <HEADER>
        <FUNC>SISNF9</FUNC>
        <USER>{ config.getString("phevos.dx.crt.request.user") }</USER>
        <PW></PW>
        <RC/>
        <RCTXT/>
      </HEADER>
      <REQUEST>
        <PARAMETER>
          <OPTION>V</OPTION>
          <SNR>{ ident.shortpartversion }</SNR>
          <AEZU_SNR>{ ident.shortrevision }</AEZU_SNR>
          <ZEICH_NR/>
          <AEZU_DOK/>
          <DOK_BLATT_NR/>
          <RANG_KZ/>
          <AEZU_HIGH/>
          <DOKART_KZ/>
          <AENDNR/>
          <VORAB_KZ/>
          <STAND></STAND>
          <TERMIN/>
          <SPRACH_KZ/>
          <PGM_NAME>MQS002</PGM_NAME>
        </PARAMETER>
      </REQUEST>
    </MQECHO-RFC>

  }
}