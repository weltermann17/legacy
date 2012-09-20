package de.man.mn.gep.scala.config.enovia5.catia5

import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.ZipOutputStream

import scala.collection.immutable.Map

import org.restlet.data.ChallengeResponse

import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.resource.d
import com.ibm.de.ebs.plm.scala.resource.disposable
import com.ibm.de.ebs.plm.scala.resource.forceContextType
import com.ibm.de.ebs.plm.scala.resource.using
import com.ibm.de.ebs.plm.scala.util.Io.addZipEntry
import com.ibm.de.ebs.plm.scala.util.Io.copyBytes

import de.man.mn.gep.scala.Server

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.partner.Partners
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory

class DeriveCATProduct(parameters: Map[String, String], authorization: ChallengeResponse)
  extends Derive3dxml(parameters, authorization) {

  override val mimetype = "application/catiaV5-Part"

  override val innerextension = "CATPart"

  override lazy implicit val zipout: ZipOutputStream = {
    val z = new JarOutputStream(out, manifestfile)
    z.setLevel(compression)
    z.setComment(comment)
    z
  }

  override def writeManifest = {
    implicit val _ = forceContextType[Unit]
    using {
      val bootstrap = disposable(Server.applicationResource("/content/BootStrapper.class", authorization).get)
      addZipEntry("BootStrapper.class") {
        copyBytes(bootstrap.getStream, zipout)
      }
      val unpack = disposable(Server.applicationResource("/content/$.exe", authorization).get)
      addZipEntry("$.exe") {
        copyBytes(unpack.getStream, zipout)
      }
    }
  }

  override def writeConfig = {
    addZipEntry("config.xml") {
      writer.print("""<?xml version="1.0" encoding="utf-8" ?>""")
      writer.print("<configuration>")
      writer.print("<general>")
      properties.get("general").foreach((j) => writeGeneralAttribute(j))
      writer.print("</general>")
      writer.print("<mapping>")
      writer.print("<append>")
      writer.print("<products>")
      properties.get("products").foreach((j) => writeAttribute(j))
      writer.print("</products>")
      writer.print("<versions>")
      properties.get("parts").foreach((j) => writeAttribute(j))
      writer.print("</versions>")
      writer.print("</append>")
      writer.print("</mapping>")
      writer.print("</configuration>")
      writer.flush
    }
    zipout.flush
  }

  override def writeBasketConfig = {
    addZipEntry("config") {
      writer.println(Json.build(Map("config" ->
        (Map("basket" -> true,
          "allcatpart" -> false,
          "log level" -> "fine",
          "log file" -> "c:\\temp\\my_log_file.log",
          "batch" -> false)))))
      writer.flush
    }
    zipout.flush
  }

  override val withUnusedReferences = false

  override def ignore(filename: String) = super.ignore(filename) || List("BootStrapper.class", "$.exe", "META-INF/MANIFEST.MF").contains(filename)

  private val manifestfile = {
    val m = new Manifest
    m.getMainAttributes.putValue("Manifest-Version", "1.0")
    m.getMainAttributes.putValue("Main-Class", "BootStrapper")
    m
  }

  def writeAttribute(submap: Json) = {
    for ((a, b) <- submap.asObject) {
      writer.print(attribute1)
      writer.print(catia1 + a + catia2)
      writer.print(enovia1 + b + enovia2)
      writer.print(attribute2)
    }
  }

  def writeGeneralAttribute(submap: Json) = {
    for ((a, b) <- submap.asObject) {
      writeMapEntry(a, b.asString)
    }
  }
  def writeMapEntry(a: String, b: String) = {
    writer.print("<" + a + ">" + b + "</" + a + ">")
  }

  val attribute1 = "<attribute>"

  val attribute2 = "</attribute>"

  val catia1 = "<catia>"

  val catia2 = "</catia>"

  val enovia1 = "<enovia>"

  val enovia2 = "</enovia>"

}
