package eu.man.phevos

package dx

package ezis

import java.io.FileOutputStream
import java.nio.file.{ Paths, Path }

import scala.util.matching.Regex

import akka.actor.{ Props, ActorRef }

import com.ibm.haploid.core.newLogger

import cc.spray.can.client.HttpClient
import cc.spray.client.{ HttpConduit, Get }
import cc.spray.http.{ StatusCodes, BasicHttpCredentials }
import cc.spray.io.IoWorker
import cc.spray.util.pimpFuture
import eu.man.phevos.dx.ezis.EzisServices.TiffInput
import eu.man.phevos.dx.util.interfaces.MTBPartFile

object EzisUtil {

  implicit val actorsystem = com.ibm.haploid.core.concurrent.actorsystem

  /**
   * Get unstamped TIFFs
   */
  def getUnstampedTiffs(workingdir: Path, tiffrequests: List[TiffInput]): List[MTBPartFile] = {
    var tifflist = List[MTBPartFile]()
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

      tiffrequests.foreach(tiffrequest ⇒ {
        val znr = new Regex("[\\.-]").replaceAllIn(tiffrequest.partnumber, "")
        val index = {
          new Regex("_").replaceAllIn(tiffrequest.versionstring, "") match {
            case "" ⇒ "-"
            case x @ _ ⇒ x
          }
        }
        val blatt = if (tiffrequest.page >= 1 && tiffrequest.page <= 9) "0" + tiffrequest.page else "" + tiffrequest.page
        val uri = ezisunstampedpath + "?Prog=" + unstampedprog + "&User=" + ezisunstampeduser + "&Typ=" + typ + "&Znr=" + znr + "&Bl=" + blatt + "&Ind=" + index

        newLogger(this).info("Download from EZIS " + ezishostname + ":" + ezisport + uri)

        // send a simple request
        val responseFuture = conduit.pipeline(Get(uri))
        val response = responseFuture.await

        if (response.status.isFailure)
          throw new Exception("Download from EZIS " + ezishostname + ":" + ezisport + uri + " failed. Bad Request. Status = " + response.status)

        val filename = {
          val filenameraw = response.headers.filter(_.name == "content-disposition")(0).value
          filenameraw.substring(filenameraw.lastIndexOf("=") + 1)
        }

        if (filename.contains(".pdf")) {
          throw new PDFFoundException("PDF file found ! PDF file requires foreign part header - perform manual data exchange")
        }

        if (response.status == StatusCodes.OK) {
          val fos: FileOutputStream = new FileOutputStream(workingdir + "/" + filename)
          fos.write(response.content.get.buffer)
          fos.close
        } else {
          throw new Exception("Bad request: " + response.status + "\nURI: " + uri + "\nMessage: " + new String(response.content.get.buffer))
        }

        val file = Paths.get(workingdir + "/" + filename)
        tifflist ++= List(MTBPartFile(file))
      })

    } finally {
      // the conduit should be closed when all operations on it have been completed
      if (null != outerconduit) outerconduit.close
      if (null != httpClient) actorsystem.stop(httpClient)
      if (null != ioWorker) ioWorker.stop
    }
    tifflist
  }

  /**
   * Get unstamped TIFF
   */
  def getUnstampedTiff(workingdir: Path, partnumber: String, page: Int, versionstring: String): MTBPartFile = {
    getUnstampedTiffs(workingdir, List(TiffInput(partnumber, versionstring, page, workingdir)))(0)
  }

}

case class PDFFoundException(reason: String) extends Exception {

}
