package de.man.mn.gep.scala.config.enovia5.catia5

import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

import scala.collection.immutable.Map

import org.restlet.data.ChallengeResponse
import org.restlet.data.CharacterSet
import org.restlet.data.Language
import org.restlet.data.MediaType
import org.restlet.representation.StringRepresentation
import org.restlet.resource.ClientResource

import com.ibm.de.ebs.plm.scala.concurrent.ops.future
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Any2Json
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2Array
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2Object
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2String
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.resource.b
import com.ibm.de.ebs.plm.scala.resource.d
import com.ibm.de.ebs.plm.scala.resource.disposable
import com.ibm.de.ebs.plm.scala.resource.forceContextType
import com.ibm.de.ebs.plm.scala.resource.using
import com.ibm.de.ebs.plm.scala.util.Io.addZipEntry
import com.ibm.de.ebs.plm.scala.util.Io.buffersize
import com.ibm.de.ebs.plm.scala.util.Io.copyBytes

import de.man.mn.gep.scala.Server

abstract class Derive(parameters: Map[String, String], authorization: ChallengeResponse) {

  val mimetype: String

  /**
   * extension for geometry inside zip file (cgr, jt, CATPart)
   */
  val innerextension: String

  val assemblyfile: String

  val assemblyextension = parameters("derivedformat")

  lazy val partner = parameters("partner")

  def ignore(filename: String): Boolean

  val hasprefix = if (parameters.contains("prefix")) "0" != parameters("prefix") else false

  val prefix: Long = if (hasprefix) makeprefix(parameters("prefix").toLong) else 0

  def writeManifest

  var properties: Json.JObject = null

  def retrieveAssembly = {
    implicit val scheduler = Server.getTaskService
    assemblyfuture = future {
      val assembly = baseuri + "assembly/" + partner + "/"
      val client = Server.applicationResource(assembly, authorization)
      val result = using {
        val representation = disposable(client.get(MediaType.APPLICATION_JSON))
        val reader = new InputStreamReader(representation.getStream)
        Json.parse(reader).get("response").asObject
      }

      properties = result.get("properties").asObject
      (result.get("instances").asArray, result.get("versions").asArray)
    }
  }

  def writeLocations = {
    locations.toList.foreach {
      case (location, filesperlocation) =>
        implicit val _ = forceContextType[Unit]
        using {
          val representation = disposable(perLocation(location, filesperlocation))
          val zipin = disposable(new ZipInputStream(representation.getStream))
          var zipentry: ZipEntry = null
          while (null != { zipentry = zipin.getNextEntry; zipentry }) {
            addZipEntry(zipentry.getName) {
              copyBytes(zipin, zipout)
              files.append(zipentry.getName)
            }
          }
        }
    }
    zipout.flush
  }

  def writeBasketConfig = {}

  def writeBasketEntries = {
    var i = 0
    entries.toList.foreach { entry =>

      i += 1
      val entryprefix = makeprefix(i)

      implicit val _ = forceContextType[Unit]
      using {
        val representation = disposable(perEntry(entry))
        val zipin = disposable(new ZipInputStream(representation.getStream))
        var zipentry: ZipEntry = null

        while (null != { zipentry = zipin.getNextEntry; zipentry }) {
          val entryname = zipentry.getName
          if ((entryname == assemblyfile) || entryname.endsWith(assemblyextension)) {
            copyBytes(zipin, entriesbuffer)
          } else if (!ignore(entryname)) {
            addZipEntry(i + "." + entryname) {
              copyBytes(zipin, zipout)
            }
          }
        }
      }
    }
    zipout.flush
  }

  def writeAssemblyHeader

  def writeAssemblyBody

  def writeBasketAssemblyBodyHeader

  def writeBasketAssemblyBody = {
    writeBasketAssemblyBodyHeader
    writer.print(entriesbuffer.toString)
  }

  def writeAssemblyFooter

  def writeAssembly = {
    try {
      addZipEntry(assemblyfile) {
        try {
          if (!hasprefix) writeAssemblyHeader
          writeAssemblyBody
          if (!hasprefix) writeAssemblyFooter
        } catch { case e => e.printStackTrace }
        writer.flush
      }
      zipout.flush
    } catch {
      case e => e.printStackTrace
    }
  }

  def writeBasketAssembly = {
    addZipEntry(assemblyfile) {
      writeAssemblyHeader
      writeBasketAssemblyBody
      writeAssemblyFooter
      writer.flush
    }
    zipout.flush
  }

  def writeAttributes(k: String, v: Json) = {}
  def writeConfig = {}

  var out: OutputStream = null

  lazy implicit val zipout = {
    val z = new ZipOutputStream(out)
    z.setLevel(compression)
    z.setComment(comment)
    z
  }

  lazy val writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(zipout, "UTF-8"), buffersize))

  var entries: Json.JArray = null

  implicit protected val _ = forceContextType[Json.JObject]

  protected lazy val (instances, versions) = assemblyfuture.get

  protected val files = new scala.collection.mutable.ListBuffer[String]

  private lazy val locations = {
    val details = baseuri + "formats/details/"
    val client = Server.applicationResource(details, authorization)
    using {
      val representation = disposable(client.get(MediaType.APPLICATION_JSON))
      val reader = new InputStreamReader(representation.getStream)
      Json.parse(reader).get("response").asObject.get("data").asObject.get(mimetype).asObject
    }
  }

  private def perLocation(location: String, filesperlocation: Json.JArray) = {
    val uritype = {
      if (baseuri.contains("/versions/")) "versions/"
      else if (baseuri.contains("/instances/")) "instances/"
      else if (baseuri.contains("/products/")) "products/"
      else if (baseuri.contains("/snapshots/")) "snapshots/"
      else if (baseuri.contains("/partnerversions/")) "partnerversions/"
      else throw new Exception("Invalid uri, type not handled " + baseuri)
    }
    val remotehost = Server.currentConfiguration(parameters, authorization).remoteHost(division, location)
    val filesuri = baseuri.substring(0, baseuri.indexOf(uritype)) + "vaults/" + location + "/derivedformats/" + parameters("derivedformat") + "/"
    val client = new ClientResource(remotehost + filesuri)
    val input = new StringRepresentation(
      Json.build(filesperlocation), MediaType.APPLICATION_JSON, Language.ALL, CharacterSet.UTF_8)
    client.setChallengeResponse(authorization)
    client.setRetryAttempts(1)
    client.setRetryDelay(500)
    client.post(input)
  }

  private def perEntry(entry: String) = {
    Server.applicationResource(entry, authorization).get
  }

  private lazy val baseuri = {
    val uri = parameters("uri")
    val b = uri.indexOf("/plm")
    val e = uri.indexOf("derivedformats/", b)
    uri.substring(b, e)
  }

  private lazy val division = {
    val b = baseuri.indexOf("divisions/") + "divisions/".length
    val e = baseuri.indexOf("/subsystems", b)
    baseuri.substring(b, e)
  }

  protected def makeprefix(prefix: Long) = {
    prefix * 1000000000.toLong
  }

  protected def unmakeprefix(prefix: Long) = {
    if (hasprefix) (prefix / 1000000000.toLong) + "." else ""
  }

  private lazy val entriesbuffer = new ByteArrayOutputStream(1 * 1024 * 1024)

  private var assemblyfuture: java.util.concurrent.Future[(Json.JArray, Json.JArray)] = null

  protected val compression = Deflater.BEST_SPEED

  protected val comment = "MAN Truck & Bus AG - generated with GEPserver/1.2"

}

