package eu.man.phevos

package dx

package gep

import java.nio.file.Path
import scala.collection.JavaConversions.asScalaBuffer
import scala.util.matching.Regex
import org.restlet.data.ChallengeScheme
import org.restlet.data.Encoding
import org.restlet.data.MediaType
import org.restlet.data.Method
import org.restlet.resource.ClientResource
import com.ibm.haploid.core.service.Failure
import com.ibm.haploid.core.service.Service
import com.ibm.haploid.core.service.ServiceException
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.util.io.copyBytes
import com.ibm.haploid.rest.client.HaploidRestClient
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import cc.spray.http.HttpContent
import cc.spray.http.HttpMethods
import cc.spray.http.HttpRequest
import cc.spray.util.pimpFuture
import util.interfaces.FileWithPath
import java.util.zip.GZIPInputStream
import java.io.FileOutputStream
import com.ibm.haploid.core.service.Result
import java.io.BufferedInputStream
import eu.man.phevos.dx.util.interfaces.PartInfo

/**
 *
 */
sealed abstract class Lookup
case class LookupFieldInput(uri: String, partindex: String, fieldname: String, where: String = "response.data") extends Lookup
case class LookupFileUrlInput(uri: String, extension: String, location: String = null) extends Lookup
case class LookupLinkInput(uri: String, partindex: String, linkname: String) extends Lookup

sealed abstract class DownloadInput
case class DownloadFileInput(url: String, @transient path: Path) extends DownloadInput
case class DownloadCATPartInput(partnumber: String, partindex: String, @transient path: Path) extends DownloadInput

sealed abstract trait DownloadResult
case class DownloadFileResult(@transient inPath: Path) extends FileWithPath(inPath) with DownloadResult
case class CATPartFileResult(@transient inPath: Path) extends FileWithPath(inPath) with DownloadResult

case class LookupTiffUrlsInput(partnumber: String, partindex: String)
case class TiffUrlResult(page: String, uri: String)

case class PartMetadataInput(partnumber: String, partindex: String) {
  def this() = this(null, null)
}

case class NativeFormats(isAssembly: Boolean, isReleased: Boolean, prcname: String, nativenames: List[String])
case class PartMetadata(isAssembly: Boolean, isReleased: Boolean, hasCATParts: Boolean, partfilename: String, hasDrawings: Boolean)

case class GEPServicesException(message: String) extends Exception(message) with ServiceException

/**
 *
 */
object GEPServices extends HaploidRestClient {

  /**
   *
   */
  object Lookup extends Service[Lookup, String] {

    def doService(in: Lookup): Result[String] = {
      Success(in match {
        case LookupFieldInput(uri, partindex, fieldname, where) ⇒
          GEPServices.lookupField(uri, partindex, fieldname, where).get
        case LookupFileUrlInput(uri, extension, location) ⇒
          GEPServices.lookupFileUrl(uri, extension, location).get
        case LookupLinkInput(uri, partindex, linkname) ⇒
          GEPServices.lookupLink(uri, partindex, linkname).get
      })
    }

  }

  object GetList extends Service[LookupTiffUrlsInput, List[TiffUrlResult]] {

    def doService(in: LookupTiffUrlsInput): Result[List[TiffUrlResult]] = {
      Success(GEPServices.lookupTiffUrls(in.partnumber, in.partindex))
    }
  }

  object GetMetadata extends Service[PartMetadataInput, PartMetadata] {

    def doService(in: PartMetadataInput): Result[PartMetadata] = {
      GEPServices.getPartMetadata(in) match {
        case Some(x) ⇒ Success(x)
        case None ⇒ Failure(new GEPServicesException("Part not found: " + in.partnumber + " " + in.partindex))
      }

    }
  }

  object DownloadFile extends Service[DownloadInput, DownloadResult] {

    def download(url: String, path: Path, method: Method = Method.GET) {
      val resource = new ClientResource("http://" + gephostname + ":" + gepport + url)
      resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, gepusername, geppassword)
      resource.setMethod(method)

      val response = {
        if (method.equals(Method.GET))
          resource.get(MediaType.APPLICATION_ALL)
        else if (method.equals(Method.HEAD))
          resource.head
        else
          resource.delete
      }

      val in = new BufferedInputStream(response.getStream)
      in.mark(2)

      var os = new FileOutputStream(path.toFile)
      
      try {
        copyBytes(new GZIPInputStream(in), os)
      } catch {
        case e: Exception =>
          os.flush
          os.close
          os = new FileOutputStream(path.toFile)
          in.reset
          copyBytes(in, os)
      }
      
      os.flush
      os.close
    }

    def doService(in: DownloadInput): Result[DownloadResult] = {

      Success(in match {
        case DownloadFileInput(url, path) ⇒ {
          download(url, path)
          DownloadFileResult(path.toAbsolutePath)
        }
        case DownloadCATPartInput(partnumber, partindex, path) ⇒ {
          val url = getCATPartUrl(partnumber, partindex)
          if (url == "") {
            throw new GEPServicesException("CATPart not found: " + partnumber + " " + partindex)
          }

          download(url, path)
          CATPartFileResult(path.toAbsolutePath)
        }
      })

    }

  }

  object GetNativeCatFormats extends Service[PartInfo, NativeFormats] {

    def doService(in: PartInfo): Result[NativeFormats] = {
      log.info("GetNativeCatFormats.doService")
      Success(GEPServices.getNativeCATFormats(in))
    }
  }

  private def findVersion(partindex: String): Config ⇒ Boolean = { config ⇒
    config.getString("versionstring").equals(partindex)
  }

  private def getFromGep(
    uri: String): HttpContent = {

    implicit val conduit = GEPServices.createConduit(gephostname, gepport)

    val request = HttpRequest(HttpMethods.GET, uri, List(getBasicAuthentificationHeader(gepusername, geppassword)))

    val responseFuture = conduit.sendReceive(request)
    val response = responseFuture.await

    val content = response.content.get
    if (null != conduit) conduit.close

    content
  }

  private def getFromGepAsConfig(
    uri: String): Config = {

    ConfigFactory.parseString(getFromGep(uri))
  }

  private def lookupField(uri: String, partindex: String, fieldname: String, where: String = "response.data"): Option[String] = {
    implicit val find = findVersion(partindex)
    implicit val select = selectString(fieldname)
    lookupField(uri, where)
  }

  private def lookupField[T](
    uri: String,
    traversable: String)(implicit find: Config ⇒ Boolean,
      select: Config ⇒ T): Option[T] = {

    val result = getFromGepAsConfig(uri).getConfigList(traversable).toList.find(find)

    if (result == None)
      return None
    else
      return Some(select(result.get))
  }

  private def lookupFileUrl(uri: String, extension: String, location: String = null): Option[String] = {
    implicit def find(element: Config): Boolean =
      element.getString("extension").equals(extension) &&
        (location == null || element.getString("location").equals(location))

    implicit val select = selectString("url")

    lookupField(uri, "response.data.formats")
  }

  private def lookupLink(uri: String, partindex: String, linkname: String): Option[String] = {
    implicit val find = findVersion(partindex)
    implicit val select = selectString("links." + linkname)

    lookupField(uri, "response.data")
  }

  private def selectString(field: String): Config ⇒ String = { config ⇒
    config.getString(field)
  }

  private def lookupTiffUrls(partnumber: String, partindex: String): List[TiffUrlResult] = {
    val url = "/plm/divisions/truck/subsystems/enovia5/versions/?like%26partname%26" + partnumber + "%26sort%26partname&from=0&to=50"
    val resultconfiglist = ConfigFactory.parseString(getFromGep(lookupLink(url, partindex, "formatssummary").get)).getConfigList("response.data.formats").filter(_.getString("extension") == "tiff")
    val resultlist = resultconfiglist.reverse.map(e ⇒ TiffUrlResult(e.getString("page"), e.getString("url"))).toList

    resultlist
  }

  private def getCATPartUrl(partnumber: String, partindex: String): String = {
    val url = "/plm/divisions/truck/subsystems/enovia5/versions/?like%26partname%26" + partnumber + "%26sort%26partname&from=0&to=50"
    val resultconfiglist = ConfigFactory.parseString(getFromGep(lookupLink(url, partindex, "formatssummary").get)).getConfigList("response.data.formats").filter(_.getString("extension") == "CATPart")

    if (resultconfiglist.size > 0) {
      val result = resultconfiglist(0).getString("url")
      result
    } else ""
  }

  private def getPartMetadata(partinput: PartMetadataInput): Option[PartMetadata] = {
    val url = "/plm/divisions/truck/subsystems/enovia5/versions/?like%26partname%26" + partinput.partnumber + "%26sort%26partname&from=0&to=50"

    val versionstring = getVersion(partinput)

    val resultconfiglist = ConfigFactory.parseString(getFromGep(url)).getConfigList("response.data").filter(_.getString("versionstring") == versionstring)
    if (resultconfiglist.size > 0) {
      val isReleased = {
        resultconfiglist(0).getString("status") match {
          case "Released" ⇒ true
          case _ ⇒ false
        }
      }

      val resultlistcatpart = ConfigFactory.parseString(getFromGep(lookupLink(url, partinput.partindex, "formatssummary").get)).getConfigList("response.data.formats").filter(_.getString("extension") == "CATPart")
      val (catpartname, hasCATParts) = {
        if (resultlistcatpart.size > 0) {
          (resultlistcatpart(0).getString("name") + "." + resultlistcatpart(0).getString("extension"), true)
        } else {
          ("", false)
        }
      }

      val hasDrawings = {
        val resultlistdrawing = ConfigFactory.parseString(getFromGep(lookupLink(url, partinput.partindex, "formatssummary").get)).getConfigList("response.data.formats").filter(_.getString("extension") == "CATDrawing")
        if (resultlistdrawing.size > 0) {
          true
        } else {
          false
        }
      }

      Option(PartMetadata(
        resultconfiglist(0).getBoolean("isassembly"),
        isReleased,
        hasCATParts,
        catpartname,
        hasDrawings))
    } else {
      None
    }
  }

  private def getNativeCATFormats(partinfo: PartInfo): NativeFormats = {
    def getURL(mtbPartNumber: String) = {
      "/plm/divisions/truck/subsystems/enovia5/versions/?like%26partname%26" + mtbPartNumber + "%26sort%26partname&from=0&to=50"
    }
    
    def getFiltered(url: String) = {
      val formatsresultlist = ConfigFactory.parseString(getFromGep(lookupLink(url, partinfo.mtbPartIndex, "formatssummary").get)).getConfigList("response.data.formats")
      formatsresultlist.filter(_.getString("extension") == "CATPart") ++ formatsresultlist.filter(_.getString("extension") == "CATDrawing")
    }
    
    val url = getURL(partinfo.mtbPartNumber);
    val filtered = getFiltered(url)
    
    val masterdata = ConfigFactory.parseString(getFromGep(url)).getConfigList("response.data").find(_.getString("versionstring").equals(partinfo.mtbPartIndex)) match {
      case Some(c) =>
        c
      case None =>
        throw new GEPServicesException("LCA object not found: " + partinfo.mtbPartNumber + " " + partinfo.mtbPartIndex)
    }

    val isAssembly = masterdata.getBoolean("isassembly")

    if (isAssembly && !masterdata.getBoolean("islatestversion")) throw new GEPServicesException("Not the latest version of the assembly : " + partinfo.mtbPartNumber + " " + partinfo.mtbPartIndex)

    val isReleased = {
      masterdata.getString("status") match {
        case "Released" ⇒ true
        case _ ⇒ false
      }
    }

    val prcname = if (isAssembly) masterdata.getString("name").toUpperCase.replace(".", "_").replace("-", "_") + "_PRC" else ""

    if (filtered.size > 0) {
      val result = new scala.collection.mutable.ListBuffer[String]

      filtered.foreach(entry ⇒ result.append(entry.getString("name") + "." + entry.getString("extension")))

      NativeFormats(isAssembly, isReleased, prcname, result.toList)

    } else NativeFormats(isAssembly, isReleased, prcname, List.empty)
  }

  private[this] def getVersion(in: PartMetadataInput): String = {
    val strippedindex = new Regex("_").replaceAllIn(in.partindex, "").trim
    strippedindex.length match {
      case 0 ⇒ {
        "___"
      }
      case 1 ⇒ {
        "_" + strippedindex + "_"
      }
      case 2 ⇒ {
        "_" + strippedindex
      }
      case 3 ⇒ {
        strippedindex
      }
    }
  }
}
