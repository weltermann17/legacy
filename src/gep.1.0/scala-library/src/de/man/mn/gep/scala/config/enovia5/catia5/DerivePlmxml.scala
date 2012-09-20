package de.man.mn.gep.scala.config.enovia5.catia5

import java.io.PrintWriter

import scala.collection.immutable.Map

import org.restlet.data.ChallengeResponse

import com.ibm.de.ebs.plm.scala.json.JsonConversions.Any2Json
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2Object
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.text.StringConversions.fromHexString
import com.ibm.de.ebs.plm.scala.util.Io.addZipEntry

class DerivePlmxml(parameters: Map[String, String], authorization: ChallengeResponse) extends Derive(parameters, authorization) {

  val mimetype = "jt"

  val innerextension = "jt"

  lazy val assemblyfile = fromHexString(parameters("nicename")).replace(".vfz", ".plmxml")

  val withUnusedReferences = false

  override def ignore(filename: String) = "open" == filename

  def writeManifest = {
    addZipEntry("open") {
      writer.println(assemblyfile)
      writer.flush
    }
    zipout.flush
  }

  def writeAssemblyHeader = {
    writer.print(header)
  }

  def writeAssemblyBody = {
    var i = 0
    instances.foreach { instance =>
      i += 1
      writeInstance(writer, instance, i)
    }
    versions.foreach { reference =>
      writeReference(writer, reference)
    }
  }

  def writeAssemblyFooter = {
    writer.print(footer)
  }

  def writeInstance(writer: PrintWriter, instance: Json.JObject, i: Int) = {
    val aggregatedby = instance.get("aggregatedby").asInt

    val instanceof = instance.get("instanceof").asInt

    if (0 < aggregatedby) {
      val part = "p" + aggregatedby
      instancerefs.get(part) match {
        case Some(z) => z.append(" " + "i" + (prefix + i))
        case None => instancerefs.put(part, new StringBuilder("i" + (prefix + i)))
      }
    }

    val transform = {
      val b = new StringBuilder
      b.append(double(instance.get("m1"))).append(" ")
      b.append(double(instance.get("m2"))).append(" ")
      b.append(double(instance.get("m3"))).append(" ").append("0 ")
      b.append(double(instance.get("m4"))).append(" ")
      b.append(double(instance.get("m5"))).append(" ")
      b.append(double(instance.get("m6"))).append(" ").append("0 ")
      b.append(double(instance.get("m7"))).append(" ")
      b.append(double(instance.get("m8"))).append(" ")
      b.append(double(instance.get("m9"))).append(" ").append("0 ")
      b.append(double(instance.get("m10"))).append(" ")
      b.append(double(instance.get("m11"))).append(" ")
      b.append(double(instance.get("m12"))).append(" ").append("1")
      b.toString
    }

    writer.print(instancepart.format("i" + (prefix + i), "p" + (prefix + instanceof), "t" + (prefix + i), transform))
  }

  def writeReference(writer: PrintWriter, reference: Json.JObject) = {
    val attributes = reference.get("attributes")
    val id = reference.get("id").asInt
    val isassembly = reference.get("isassembly").asBoolean

    if (isassembly) {
      val instanceref = instancerefs.getOrElse("p" + id, "").toString
      assembly(writer, (prefix + id), reference, instanceref)
    } else {
      part(writer, (prefix + id), reference)
    }
  }

  override def writeBasketAssemblyBodyHeader = {
    writer.print(instancepart.format("i1", "p1", "t1", " 1 0 0 0 0 1 0 0 0 0 1 0 0 0 0 0"))

    var i = 0
    val instanceref = new StringBuilder
    entries.toList.foreach { entry =>
      i += 1
      val entryprefix = "i" + (makeprefix(i) + i)
      instanceref.append(" ").append(entryprefix)
    }

    writer.print(ProductRevisionView.format("p1", fromHexString(parameters("nicename")), instanceref, "u1", fromHexString(parameters("nicename"))))
  }

  private def double(json: Option[Json]): String = {
    val epsilon = 1e-15
    json match {
      case Some(d) => if (epsilon > scala.math.abs(d.asDouble)) "0" else if (1. == d.asDouble) "1" else d.asDouble.toString
      case _ => "0"
    }
  }

  private def escape(s: String) = s.replace("&", "&amp;").replace(">", "&gt;").replace("<", "&lt;").replace("\"", "&quot;").replace("'", "&apos;")

  private val instancerefs = new scala.collection.mutable.HashMap[String, StringBuilder]

  private val header = """<?xml version="1.0" encoding="utf-8" ?><PLMXML><Header><Generator>GEPserver/1.0</Generator></Header><ProductDef id="pd1" name=""><InstanceGraph id="ig1" rootInstanceRef="i1">"""

  private def part(writer: PrintWriter, id: Long, reference: Json.JObject) = {
    val displayname = escape(reference.get("displayname").asString)
    val filename = escape(reference.get("filename").asString + "." + innerextension)
    val attributes = reference.get("attributes")
    val location = if (files.contains(filename)) unmakeprefix(prefix) + filename else ""

    writer.print("""<ProductRevisionView id="p""" + id + """" name="""" + displayname + """">""")
    writer.print("""<Representation id="r""" + id + """" format="JT" location="""" + location + """"></Representation>""")
    writer.print("""<UserData id="u""" + id + """">""")

    writeUserData(writer, attributes)

    writer.print("""</UserData>""")
    writer.print("""</ProductRevisionView>""")
  }

  private def assembly(writer: PrintWriter, id: Long, reference: Json.JObject, instanceref: String) = {
    val displayname = escape(reference.get("displayname").asString)
    val attributes = reference.get("attributes")

    writer.print("""<ProductRevisionView id="p""" + id + """" name="""" + displayname + """" type="assembly" instanceRefs="""" + instanceref + """">""")
    writer.print("""<UserData id="u""" + id + """">""")

    writeUserData(writer, attributes)

    writer.print("</UserData>")
    writer.print("</ProductRevisionView>")
  }

  private def writeUserData(writer: PrintWriter, userdef: Json) = {
    for ((k, v) <- userdef) writer.print("""<UserValue title="""" + escape(k) + """" value="""" + escape(v.toString) + """"></UserValue>""")
  }

  private val instancepart = """<ProductInstance id="%1$s" partRef="%2$s"><Transform id="%3$s"> %4$s</Transform></ProductInstance>"""

  private val footer = """</InstanceGraph></ProductDef></PLMXML>"""

  private val ProductRevisionView = """<ProductRevisionView id="%1$s" name="%2$s"  type="assembly" instanceRefs="%3$s"><UserData id="%4$s"><UserValue title="Nomenclature:" value="%5$s"></UserValue></UserData></ProductRevisionView>"""

}
