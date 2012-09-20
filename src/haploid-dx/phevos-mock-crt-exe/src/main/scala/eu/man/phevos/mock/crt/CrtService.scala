package eu.man.phevos.mock.crt
import com.ibm.haploid.rest.HaploidRestServer
import com.ibm.haploid.rest.HaploidService
import com.ibm.haploid.core.util.io._

import javax.jms._
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.broker.BrokerService
import org.apache.ftpserver._
import org.apache.ftpserver.listener._
import org.apache.ftpserver.usermanager._
import org.apache.ftpserver.usermanager.impl.BaseUser
import java.util.Calendar
import java.text.SimpleDateFormat
import java.io.File
import scala.xml._
import scala.actors._
import scala.tools.nsc.io.Directory
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

object Main extends App with HaploidRestServer {

  override def onTermination = {

    consumer.close

    connection.close

    MqBroker ! Stop

    FtpServer ! Stop

    0
  }

  debug("CRT JMS MQ broker url : " + mq_broker_url)
  debug("CRT JMS client name : " + mq_clientname)
  debug("CRT JMS request queue : " + mq_request_queue)
  debug("CRT JMS reply queue : " + mq_response_queue)
  debug("use embedded jms broker : " + embedded_mqbroker_enabled)
  debug("use embedded ftp server : " + embedded_ftpserver_enabled)

  if (embedded_mqbroker_enabled) MqBroker.start

  if (embedded_ftpserver_enabled) FtpServer.start

  val connectionFactory = new ActiveMQConnectionFactory(mq_broker_url)

  val connection = connectionFactory.createConnection
  connection.setClientID(mq_clientname)
  connection.start
  debug("CRT mockup connected to ActiveMQ")

  val session: Session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
  val inputqueue = session.createQueue(mq_request_queue)
  val consumer = session.createConsumer(inputqueue)
  debug("CRT mockup created message consumer")

  val listener = new MessageListener {

    def onMessage(message: Message) {

      message match {

        case text: TextMessage ⇒ {
          val request = text.getText
          debug("CRT mockup; received message id: " + text.getJMSCorrelationID)

          val outputqueue = session.createQueue(mq_response_queue)
          val producer = session.createProducer(outputqueue)

          val reply = session.createTextMessage(getResponse(request))

          reply.setJMSCorrelationID(text.getJMSCorrelationID)

          debug("CRT mockup; send message id: " + reply.getJMSCorrelationID)

          producer.send(reply)
          producer.close
        }

        case _ ⇒ warning("unsupported message type")

      }

    }

  }

  consumer.setMessageListener(listener)
  debug("CRT mockup set up listener to queue: " + mq_request_queue)
  info("CRT mockup alive")

  def getResponse(message: String): String = {
    val destination = new java.io.File(getClass.getProtectionDomain.getCodeSource.getLocation.getPath).getParentFile

    debug("request : " + message)

    val request = XML.loadString(message)

    val file = destination.toPath + "/mqs" + "/MQS_" + (request \ "REQUEST" \ "E_KEY" \ "MTB_IDENT").text + "_answer.xml"
    debug("-------------------------------------------")
    debug("used response file : " + file)

    val response = XML.load(file)

    debug("response : " + response.toString)
    debug("-------------------------------------------")
    val funktion = (request \\ "FUNKTION").text
    info("'funktion': " + funktion)

    val mtbident = (request \\ "MTB_IDENT").text
    info("'mtb_ident': " + mtbident)

    val output = funktion.toUpperCase match {
      case "C" ⇒
        {
          if ((response \ "ANSWER" \ "A_INDEX" \ "DRAWING_DATE").text == "") {
            updateAEDAT(updateRC(updateDrawingDate(updateTitleBlock(response, "Y"), getDatestamp), "00"))
          } else {
            updateAEDAT(updateRC(updateTitleBlock(response, "Y"), "00"))
          }
        }

      case "1" ⇒ {
        val old_state = (response \ "ANSWER" \ "A_INDEX" \ "DX_STATUS").text

        old_state match {
          case "03" | "04" ⇒ updateAEDAT(updateRC(updateDxStatus(response, "05"), "00"))
          case _ ⇒ setErrorMsg(response, "invalid value for DXStatus : " + old_state)
        }

      }

      case "2" ⇒ {
        val old_drawingdate = (response \ "ANSWER" \ "A_INDEX" \ "DRAWING_DATE").text
        val old_state = (response \ "ANSWER" \ "A_INDEX" \ "DX_STATUS").text

        if (old_drawingdate == "") {
          if (old_state == "05") {
            val partnerrevision = (request \\ "E_KEY" \\ "P_REVISION").text
            val newresponse = updatePartnerRevision(updateDxStatus(response, "06"), partnerrevision)

            updateAEDAT(updateRC(updateDrawingDate(newresponse, getDatestamp), "00"))
          } else {
            setErrorMsg(response, "invalid value for DXStatus : " + old_state)
          }
        } else {
          setErrorMsg(response, "drawing date allready set")
        }

      }

      case "U" ⇒ updateRC(updateTS(updateAEDAT(response)), "00")

      case "S" ⇒ updateRC(response, "00")

      case _ ⇒ setErrorMsg(response, "unknown function")
    }

    XML.save(file.toString, output, "UTF-8", false, null)
    output.toString
  }

  def updateAEDAT(node: Node): Node = {
    def updateElements(seq: Seq[Node]): Seq[Node] =
      for (subNode ← seq) yield updateAEDAT(subNode)

    node match {
      case <MQERFC>{ ch @ _* }</MQERFC> ⇒ <MQERFC>{ updateElements(ch) }</MQERFC>
      case <ANSWER>{ ch @ _* }</ANSWER> ⇒ <ANSWER>{ updateElements(ch) }</ANSWER>
      case <A_IDENT>{ ch @ _* }</A_IDENT> ⇒ <A_IDENT>{ updateElements(ch) }</A_IDENT>
      case <AEDAT>{ ch @ _* }</AEDAT> ⇒ <AEDAT>{ getTimestamp }</AEDAT>
      case other @ _ ⇒ other
    }
  }

  def updateTS(node: Node): Node = {
    def updateElements(seq: Seq[Node]): Seq[Node] =
      for (subNode ← seq) yield updateTS(subNode)

    node match {
      case <MQERFC>{ ch @ _* }</MQERFC> ⇒ <MQERFC>{ updateElements(ch) }</MQERFC>
      case <HEADER>{ ch @ _* }</HEADER> ⇒ <HEADER>{ updateElements(ch) }</HEADER>
      case <TS>{ ch @ _* }</TS> ⇒ <TS>{ getTimestamp }</TS>
      case other @ _ ⇒ other
    }
  }

  def updateDrawingDate(node: Node, date: String): Node = {
    def updateElements(seq: Seq[Node]): Seq[Node] =
      for (subNode ← seq) yield updateDrawingDate(subNode, date)

    node match {
      case <MQERFC>{ ch @ _* }</MQERFC> ⇒ <MQERFC>{ updateElements(ch) }</MQERFC>
      case <ANSWER>{ ch @ _* }</ANSWER> ⇒ <ANSWER>{ updateElements(ch) }</ANSWER>
      case <A_INDEX>{ ch @ _* }</A_INDEX> ⇒ <A_INDEX>{ updateElements(ch) }</A_INDEX>
      case <DRAWING_DATE>{ ch @ _* }</DRAWING_DATE> ⇒ <DRAWING_DATE>{ date }</DRAWING_DATE>
      case other @ _ ⇒ other
    }
  }

  def updateTitleBlock(node: Node, hasTitleblock: String): Node = {
    def updateElements(seq: Seq[Node]): Seq[Node] =
      for (subNode ← seq) yield updateTitleBlock(subNode, hasTitleblock)

    node match {
      case <MQERFC>{ ch @ _* }</MQERFC> ⇒ <MQERFC>{ updateElements(ch) }</MQERFC>
      case <ANSWER>{ ch @ _* }</ANSWER> ⇒ <ANSWER>{ updateElements(ch) }</ANSWER>
      case <A_INDEX>{ ch @ _* }</A_INDEX> ⇒ <A_INDEX>{ updateElements(ch) }</A_INDEX>
      case <P_TITLE_BLOCK>{ ch @ _* }</P_TITLE_BLOCK> ⇒ <P_TITLE_BLOCK>{ hasTitleblock }</P_TITLE_BLOCK>
      case other @ _ ⇒ other
    }
  }

  def updateDxStatus(node: Node, status: String) = {

    def updateDxState(node: Node, status: String): Node = {
      def updateElements(seq: Seq[Node]): Seq[Node] =
        for (subNode ← seq) yield updateDxState(subNode, status)

      node match {
        case <MQERFC>{ ch @ _* }</MQERFC> ⇒ <MQERFC>{ updateElements(ch) }</MQERFC>
        case <ANSWER>{ ch @ _* }</ANSWER> ⇒ <ANSWER>{ updateElements(ch) }</ANSWER>
        case <A_INDEX>{ ch @ _* }</A_INDEX> ⇒ <A_INDEX>{ updateElements(ch) }</A_INDEX>
        case <DX_STATUS>{ ch @ _* }</DX_STATUS> ⇒ <DX_STATUS>{ status }</DX_STATUS>
        case other @ _ ⇒ other
      }
    }

    def updateDxStateTimeStmp(node: Node): Node = {
      def updateElements(seq: Seq[Node]): Seq[Node] =
        for (subNode ← seq) yield updateDxStateTimeStmp(subNode)

      node match {
        case <MQERFC>{ ch @ _* }</MQERFC> ⇒ <MQERFC>{ updateElements(ch) }</MQERFC>
        case <ANSWER>{ ch @ _* }</ANSWER> ⇒ <ANSWER>{ updateElements(ch) }</ANSWER>
        case <A_INDEX>{ ch @ _* }</A_INDEX> ⇒ <A_INDEX>{ updateElements(ch) }</A_INDEX>
        case <DX_STATUS_TSMP>{ ch @ _* }</DX_STATUS_TSMP> ⇒ <DX_STATUS_TSMP>{ getTimestamp }</DX_STATUS_TSMP>
        case other @ _ ⇒ other
      }
    }

    updateDxStateTimeStmp(updateDxState(node, status))
  }

  def updateRC(node: Node, rc: String): Node = {
    def updateElements(seq: Seq[Node]): Seq[Node] =
      for (subNode ← seq) yield updateRC(subNode, rc)

    node match {
      case <MQERFC>{ ch @ _* }</MQERFC> ⇒ <MQERFC>{ updateElements(ch) }</MQERFC>
      case <ANSWER>{ ch @ _* }</ANSWER> ⇒ <ANSWER>{ updateElements(ch) }</ANSWER>
      case <A_PARM>{ ch @ _* }</A_PARM> ⇒ <A_PARM>{ updateElements(ch) }</A_PARM>
      case <RC>{ ch @ _* }</RC> ⇒ <RC>{ rc }</RC>
      case other @ _ ⇒ other
    }
  }

  def updatePartnerRevision(node: Node, revision: String): Node = {
    def updateElements(seq: Seq[Node]): Seq[Node] =
      for (subNode ← seq) yield updatePartnerRevision(subNode, revision)

    node match {
      case <MQERFC>{ ch @ _* }</MQERFC> ⇒ <MQERFC>{ updateElements(ch) }</MQERFC>
      case <ANSWER>{ ch @ _* }</ANSWER> ⇒ <ANSWER>{ updateElements(ch) }</ANSWER>
      case <A_INDEX>{ ch @ _* }</A_INDEX> ⇒ <A_INDEX>{ updateElements(ch) }</A_INDEX>
      case <RC>{ ch @ _* }</RC> ⇒ <RC>{ revision }</RC>
      case other @ _ ⇒ other
    }
  }
  def setErrorMsg(node: Node, message: String) = {

    def updateFText(node: Node, text: String): Node = {
      def updateElements(seq: Seq[Node]): Seq[Node] =
        for (subNode ← seq) yield updateFText(subNode, text)

      node match {
        case <MQERFC>{ ch @ _* }</MQERFC> ⇒ <MQERFC>{ updateElements(ch) }</MQERFC>
        case <ANSWER>{ ch @ _* }</ANSWER> ⇒ <ANSWER>{ updateElements(ch) }</ANSWER>
        case <A_PARM>{ ch @ _* }</A_PARM> ⇒ <A_PARM>{ updateElements(ch) }</A_PARM>
        case <FTEXT>{ ch @ _* }</FTEXT> ⇒ <FTEXT>{ text }</FTEXT>
        case other @ _ ⇒ other
      }
    }

    updateRC(updateFText(node, message), "01")
  }

  private def getTimestamp: String = {
    val now = Calendar.getInstance
    val dateformat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS")
    dateformat.format(now.getTime)
  }

  private def getDatestamp: String = {
    val now = Calendar.getInstance
    val dateformat = new SimpleDateFormat("dd.MM.yyyy")
    dateformat.format(now.getTime)
  }
}

object MqBroker extends Actor {

  def act() {
    val activemq = new BrokerService
    val destination = new java.io.File(getClass.getProtectionDomain.getCodeSource.getLocation.getPath).getParentFile.getPath

    debug("MQ root folder : " + destination + "/" + "mqs")

    copyResources("mqs", destination)
    try {
      activemq.addConnector(mq_broker_url);
      activemq.start

      info("CRT Mockup MQ broker started on: " + mq_broker_url)
    } catch {
      case e: Throwable ⇒ error("unable to start ActiveMQ message broker")
    }

    receive {
      case Stop ⇒ {
        info("stopping ActiveMQ broker on url '" + mq_broker_url + "'")
        activemq.stop
      }
    }
  }
}

object FtpServer extends Actor {
  def act() {
    val srvfact = new FtpServerFactory
    val usrmgrfact = new PropertiesUserManagerFactory
    val conconffact = new ConnectionConfigFactory
    val listfact = new ListenerFactory

    conconffact.setAnonymousLoginEnabled(true)
    val conconf = conconffact.createConnectionConfig
    srvfact.setConnectionConfig(conconf)

    val destination = new java.io.File(getClass.getProtectionDomain.getCodeSource.getLocation.getPath).getParentFile.getPath

    debug("FTP root folder : " + destination + "/" + ftp_rootfolder)

    copyResources(ftp_rootfolder, destination)

    val user = new BaseUser

    user.setName(ftp_user)
    user.setPassword(ftp_password)
    user.setHomeDirectory(destination + "/" + ftp_rootfolder)

    val usrmgr = usrmgrfact.createUserManager
    usrmgr.save(user)
    srvfact.setUserManager(usrmgr)

    val dataconconffact = new DataConnectionConfigurationFactory
    dataconconffact.setPassivePorts(ftp_passiveports)

    listfact.setDataConnectionConfiguration(dataconconffact.createDataConnectionConfiguration)
    listfact.setPort(ftp_port)

    srvfact.addListener("default", listfact.createListener())
    val ftpserver = srvfact.createServer

    ftpserver.start
    info("CRT Mockup FTP server started")

    receive {
      case Stop ⇒
        {
          info("stopping CRT Mockup FTP server")
        }
        ftpserver.stop
    }
  }
}

case object Stop

class CrtService extends HaploidService {

  lazy val service = {
    val page = "CRT MockUp Service, Folder : " + (new java.io.File(getClass.getProtectionDomain.getCodeSource.getLocation.getPath).getParentFile).getAbsolutePath
    completeWith(page)
  }
}