package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document

import java.io.PrintWriter

import org.restlet.data.MediaType

import com.ibm.de.ebs.plm.scala.database.Raw
import com.ibm.de.ebs.plm.scala.rest.Services
import com.ibm.de.ebs.plm.scala.util.Converters.bytesToKb
import com.ibm.de.ebs.plm.scala.util.Converters.bytesToMb
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper
import com.ibm.de.ebs.plm.scala.json.Json._

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.Products
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.Versions
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.Instances
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryRepresentation
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository

class FormatsDetailsInMemory
  extends InMemoryRepresentation {

  override def doWriteInMemory(writer: PrintWriter) = {
    val v = datatype match {
      case "versions" =>
        val v = versions(Raw(parameters("version")))
        versions.versions(v)
      case "instances" =>
        val i = Instances(Raw(parameters("instance")))
        val v = i.version(0)
        versions.versions(v)
      case "products" =>
        val p = products(Raw(parameters("product")))
        products.versions(p)
    }
    v.foreach(fillDetails)

    writer.print("{\"response\":{\"data\":{")
    var i = 0
    mimetypes.toList.foreach {
      case (mimetype, vaults) =>
        if (0 < i) writer.print(",")
        writer.print("\"")
        writer.print(mimetype)
        writer.print("\":{")
        var j = 0
        vaults.toList.foreach {
          case (vault, details) =>
            if (0 < j) writer.print(",")
            writer.print("\"")
            writer.print(vault)
            writer.print("\":[")
            var k = 0
            details.foreach {
              case (filename, filepath) =>
                if (0 < k) writer.print(",")
                writer.print("{\"filename\":\"")
                writer.print(filename)
                writer.print("\",\"filepath\":\"")
                writer.print(filepath)
                writer.print("\"}")
                k += 1
            }
            writer.print("]")
            j += 1
        }
        writer.print("}")
        i += 1
    }
    writer.print("},\"status\":0}}")
  }

  private def fillDetails(version: Int) = {
    versions.documents(version).foreach { j =>
      documents.formats(j).foreach { i =>
        val mimetype = formats.mimetype(i)
        val vault = formats.vault(i).substring(0, 3)
        val vaults = mimetypes.get(mimetype) match {
          case None => val v = new Vaults; mimetypes.put(mimetype, v); v
          case Some(v) => v
        }
        val details = vaults.get(vault) match {
          case None => val d = new Details; vaults.put(vault, d); d
          case Some(d) => d
        }
        val filename = Services.Metadata.getExtension(MediaType.valueOf(mimetype)) match {
          case ext if "cgr" == ext || "jt" == ext || "CATPart" == ext => versions.filename(version) + "." + ext
          case ext if "3dxml" == ext => versions.filename(version) + ".cgr"
          case ext => documents.name(j) + "." + ext
        }
        val filepath = formats.filepath(i).replace("/", "\\/")
        details += ((filename, filepath))
      }
    }
  }

  private type FormatDetails = (String, String)
  private type Details = collection.mutable.ListBuffer[FormatDetails]
  private type Vaults = collection.mutable.HashMap[String, Details]
  private type MimeTypes = collection.mutable.HashMap[String, Vaults]

  private val mimetypes = new MimeTypes
  private lazy val products = Repository(classOf[Products])
  private lazy val versions = Repository(classOf[Versions])
  private lazy val documents = Repository(classOf[Documents])
  private lazy val formats = Repository(classOf[Formats])

}