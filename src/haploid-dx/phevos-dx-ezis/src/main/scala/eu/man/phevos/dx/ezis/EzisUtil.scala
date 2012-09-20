package eu.man.phevos

package dx

package ezis

import java.io.FileOutputStream
import java.nio.file.{ Paths, Path }

import scala.util.matching.Regex

import akka.actor.{ Props, ActorRef }

import cc.spray.can.client.HttpClient
import cc.spray.client.{ HttpConduit, Get }
import cc.spray.http.{ StatusCodes, BasicHttpCredentials }
import cc.spray.io.IoWorker
import cc.spray.util.pimpFuture
import eu.man.phevos.dx.ezis.EzisServices.TiffFile

object EzisUtil {

  implicit val actorsystem = com.ibm.haploid.core.concurrent.actorsystem

  /**
   * get an unstamped TIFF
   */
  def getUnstampedTiff(workingdir: Path, partnumber: String, page: Int, versionstring: String): TiffFile = {
    var ioWorker: IoWorker = null
    var httpClient: ActorRef = null
    var outerconduit: HttpConduit = null
    try {
      // every spray-can HttpClient (and HttpServer) needs an IoWorker for low-level network IO
      // (but several servers and/or clients can share one)
      ioWorker = new IoWorker(actorsystem).start

      // create and start a spray-can HttpClient
      httpClient = actorsystem.actorOf(
        props = Props(new HttpClient(ioWorker)),
        name = "http-client-" + com.ibm.haploid.core.util.Uuid.newUuid)

      val znr = new Regex("[\\.-]").replaceAllIn(partnumber, "")
      val index = {
        new Regex("_").replaceAllIn(versionstring, "") match {
          case "" ⇒ "-"
          case x @ _ ⇒ x
        }
      }
      val blatt = if (page >= 1 && page <= 9) "0" + page else "" + page
      val uri = ezisunstampedpath + "?Prog=" + unstampedprog + "&User=" + ezisunstampeduser + "&Typ=" + typ + "&Znr=" + znr + "&Bl=" + blatt + "&Ind=" + index

      val conduit = {
        ezisport match {
          case x if (x != 80) ⇒
            new HttpConduit(httpClient, ezishostname, ezisport) {
              val pipeline = (
                simpleRequest ~> authenticate(BasicHttpCredentials(ezisunstampedusername, ezisunstampedpassword))) ~> sendReceive
            }
          case _ ⇒
            new HttpConduit(httpClient, ezishostname) {
              val pipeline = (
                simpleRequest ~> authenticate(BasicHttpCredentials(ezisunstampedusername, ezisunstampedpassword))) ~> sendReceive
            }
        }
      }
      outerconduit = conduit

      // send a simple request
      val responseFuture = conduit.pipeline(Get(uri))
      val response = responseFuture.await

      val filename = znr + "_" + (if (page >= 1 && page <= 9) "0") + page + "_" + versionstring + ".tif"

      if (response.status == StatusCodes.OK) {
        val fos: FileOutputStream = new FileOutputStream(workingdir + "/" + filename);
        fos.write(response.content.get.buffer)
        fos.close
      } else {
        throw new Exception("Bad request: " + response.status + "\nURI: " + uri + "\nMessage: " + new String(response.content.get.buffer))
      }
      val file = Paths.get(workingdir + "/" + filename)
      TiffFile(file)
    } finally {
      // the conduit should be closed when all operations on it have been completed
      if (null != outerconduit) outerconduit.close
      if (null != httpClient) actorsystem.stop(httpClient)
      if (null != ioWorker) ioWorker.stop
    }
  }

  /**
   * get a stamped TIFF
   */
  def getStampedTiff(workingdir: Path, partnumber: String, page: Int, versionstring: String): TiffFile = {
    var ioWorker: IoWorker = null
    var httpClient: ActorRef = null
    var outerconduit: HttpConduit = null
    try {
      // every spray-can HttpClient (and HttpServer) needs an IoWorker for low-level network IO
      // (but several servers and/or clients can share one)
      ioWorker = new IoWorker(actorsystem).start

      // create and start a spray-can HttpClient
      httpClient = actorsystem.actorOf(
        props = Props(new HttpClient(ioWorker)),
        name = "http-client-" + com.ibm.haploid.core.util.Uuid.newUuid)

      val znr = new Regex("[\\.-]").replaceAllIn(partnumber, "")
      val index = {
        new Regex("_").replaceAllIn(versionstring, "") match {
          case "" ⇒ "-"
          case x @ _ ⇒ x
        }
      }
      val blatt = if (page >= 1 && page <= 9) "0" + page else "" + page
      val uri = ezisstampedpath + "?Prog=" + stampedprog + "&User=" + ezisstampeduser + "&Typ=" + typ + "&Znr=" + znr + "&Bl=" + blatt + "&Ind=" + index

      val conduit = {
        ezisport match {
          case x if (x != 80) ⇒
            new HttpConduit(httpClient, ezishostname, ezisport) {
              val pipeline = (
                simpleRequest ~> authenticate(BasicHttpCredentials(ezisunstampedusername, ezisunstampedpassword))) ~> sendReceive
            }
          case _ ⇒
            new HttpConduit(httpClient, ezishostname) {
              val pipeline = (
                simpleRequest ~> authenticate(BasicHttpCredentials(ezisunstampedusername, ezisunstampedpassword))) ~> sendReceive
            }
        }
      }
      outerconduit = conduit

      // send a simple request
      val responseFuture = conduit.pipeline(Get(uri))
      val response = responseFuture.await

      val filename = znr + "_" + (if (page >= 1 && page <= 9) "0") + page + "_" + versionstring + ".tif"

      if (response.status == StatusCodes.OK) {
        val fos: FileOutputStream = new FileOutputStream(workingdir + "/" + filename);
        fos.write(response.content.get.buffer)
        fos.close
      } else {
        throw new Exception("Bad request: " + response.status + "\nURI: " + uri + "\nMessage: " + new String(response.content.get.buffer))
      }

      val file = Paths.get(workingdir + "/" + filename)
      TiffFile(file)
    } finally {
      // the conduit should be closed when all operations on it have been completed
      if (null != outerconduit) outerconduit.close
      if (null != httpClient) actorsystem.stop(httpClient)
      if (null != ioWorker) ioWorker.stop
    }
  }

}