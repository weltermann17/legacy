package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document

import java.io.PrintWriter
import org.restlet.data.MediaType
import com.ibm.de.ebs.plm.scala.database.Raw
import com.ibm.de.ebs.plm.scala.rest.Services
import com.ibm.de.ebs.plm.scala.text.StringConversions.toHexString
import com.ibm.de.ebs.plm.scala.util.Converters.bytesToKb
import com.ibm.de.ebs.plm.scala.util.Converters.bytesToMb
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.Products
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.Versions
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.Instances
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryRepresentation
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Column

class FormatsSummaryInMemory extends InMemoryRepresentation {

  type T = InMemoryTable { val isassembly: Column[Boolean]; val filename: Column[String] }

  override def doWriteInMemory(writer: PrintWriter) = {

    val (children, root, roottable, template, versionroot) = datatype match {
      case "versions" =>
        val v = versions(Raw(parameters("version")))
        (versions.versions(v), v, versions.asInstanceOf[T], "{version}", v)
      case "instances" =>
        val i = Instances(Raw(parameters("instance")))
        (i.versions(0), 0, i.asInstanceOf[T], "{instance}", i.version(0))
      case "products" =>
        val p = products(Raw(parameters("product")))
        (products.versions(p), p, products.asInstanceOf[T], "{product}", -1)
    }

    children.foreach(fillSummary)

    writer.print("{\"response\":{\"data\":{\"formats\":[")
    var i = 0

    if (roottable.isassembly(root)) {
      fillDerived(root, roottable, template)
      derived.foreach { f =>
        if (0 < i) writer.print(",")
        f.print(writer)
        i += 1
      }
    }

    fillNative(versionroot, template)
    natives.foreach { f =>
      if (0 < i) writer.print(",")
      f.print(writer)
      i += 1
    }

    writer.print("],\"summary\":[")
    i = 0
    formatssummary.values.toList.sortWith { case (a, b) => a.filesize > b.filesize }.foreach { f =>
      if (0 < i) writer.print(",")
      f.print(writer)
      i += 1
    }
    writer.print("]")
    writer.print("},\"startRow\":")
    writer.print(0)
    writer.print(",\"endRow\":")
    writer.print(formatssummary.size)
    writer.print(",\"totalRows\":")
    writer.print(formatssummary.size)
    writer.print(",\"status\":0}}")
  }

  private def fillSummary(root: Int) = {
    versions.documents(root).foreach(documents.formats(_).foreach { i =>
      val key = (formats.mimetype(i), formats.vault(i).substring(0, 3))
      val formatpervault = formatssummary.get(key) match {
        case None => val f = FormatPerVault(key._1, key._2); formatssummary.put(key, f); f
        case Some(f) => f
      }
      formatpervault.filesize += formats.filesize(i)
    })
  }

  private def fillDerived(root: Int, roottable: T, template: String) = {
    List("CATProduct.jar", "3dxml", "vfz").foreach(e => derived.append(Derived(root, roottable, template, e)))
  }

  private def fillNative(root: Int, template: String) = {
    if (-1 < root) {
      versions.documents(root).foreach(i => documents.formats(i).foreach { j =>
        val f = Native(
          documents.name(i),
          documents.id(i).toString,
          formats.filepath(j),
          formats.mimetype(j),
          documents.versionstring(i),
          formats.vault(j),
          formats.filesize(j).toLong)
        natives.append(f)
      })
    }
  }

  private case class FormatPerVault(
    mimetype: String,
    vault: String) {
    var filesize: Long = 0
    def filesize_kb = bytesToKb(filesize, 0).toLong
    def filesize_mb = bytesToMb(filesize).toLong
    val extension = Services.Metadata.getExtension(MediaType.valueOf(mimetype))

    def print(writer: PrintWriter) = {
      writer.print("{\"mimetype\":\"")
      writer.print(mimetype)
      writer.print("\",\"vault\":\"")
      writer.print(vault)
      writer.print("\",\"filesize\":")
      writer.print(filesize)
      writer.print(",\"filesize_kb\":")
      writer.print(filesize_kb)
      writer.print(",\"filesize_mb\":")
      writer.print(filesize_mb)
      writer.print(",\"extension\":\"")
      writer.print(extension)
      writer.print("\"}")
    }
  }

  private case class Native(
    name: String,
    documentid: String,
    filepath: String,
    mimetype: String,
    version: String,
    vault: String,
    filesize: Long) {

    lazy val location = vault.substring(0, 3).toLowerCase
    lazy val extension = Services.Metadata.getExtension(MediaType.valueOf(mimetype))
    lazy val nativeformat = extension.toLowerCase
    lazy val nicename = name + version
    lazy val url = baseuri.substring(0, baseuri.indexOf("/" + datatype + "/")) + "/vaults/" + location + "/nativeformats/" + nativeformat + "/" + documentid + "/" + toHexString(filepath) + "/" + toHexString(nicename + "." + extension) + "/"
    def filesize_kb = bytesToKb(filesize, 0).toLong
    def filesize_mb = bytesToMb(filesize).toLong
    lazy val page = try { nicename.substring(18, 22) } catch { case e => "0001" }

    def print(writer: PrintWriter) = {
      writer.print("{\"location\":\"")
      writer.print(location)
      writer.print("\",\"nativeformat\":\"")
      writer.print(nativeformat)
      writer.print("\",\"url\":\"")
      writer.print(url)
      writer.print("\",\"name\":\"")
      writer.print(nicename)
      writer.print("\",\"page\":\"")
      writer.print(page)
      writer.print("\",\"mimetype\":\"")
      writer.print(mimetype)
      writer.print("\",\"vault\":\"")
      writer.print(vault)
      writer.print("\",\"filesize\":")
      writer.print(filesize)
      writer.print(",\"filesize_kb\":")
      writer.print(filesize_kb)
      writer.print(",\"filesize_mb\":")
      writer.print(filesize_mb)
      writer.print(",\"extension\":\"")
      writer.print(extension)
      writer.print("\"}")
    }

  }

  private case class Derived(
    root: Int,
    roottable: T,
    template: String,
    extension: String) {

    lazy val derivedformat = extension match {
      case "CATProduct.jar" => "catproduct"
      case "3dxml" => "3dxml"
      case "vfz" => "plmxml"
      case f => throw new Exception("Unknown derived format: " + f)
    }

    lazy val nicename = {
      val nname = roottable.filename(root)
      datatype match {
        case "versions" | "instances" =>
          val cadtype = extension match {
            case "CATProduct.jar" => "_G3D_"
            case "3dxml" => "_DMU_"
            case "vfz" => "_LHT_"
            case _ => "_G3D_"
          }
          nname.replace("_TYP_", cadtype)
        case _ => nname
      }
    }

    lazy val url = {
      val url = baseuri.replace(template, roottable.id(root).toString)
      url.substring(0, url.indexOf("formats/")) +
        "derivedformats/" +
        derivedformat +
        "/" +
        toHexString(nicename + "." + extension) +
        "/E137A1C70221436FB881EE2773787EE2/0/"
    }

    def print(writer: PrintWriter) = {
      writer.print("{\"filesize\": -1,")
      writer.print("\"name\": \"\",")
      writer.print("\"url\":\"")
      writer.print(url)
      writer.print("\",\"derivedformat\":\"")
      writer.print(derivedformat)
      writer.print("\",\"extension\":\"")
      writer.print(extension)
      writer.print("\"}")
    }
  }

  private val formatssummary = new collection.mutable.HashMap[(String, String), FormatPerVault]
  private val derived = new collection.mutable.ListBuffer[Derived]
  private val natives = new collection.mutable.ListBuffer[Native]

  private lazy val products = Repository(classOf[Products])
  private lazy val versions = Repository(classOf[Versions])
  private lazy val documents = Repository(classOf[Documents])
  private lazy val formats = Repository(classOf[Formats])

}