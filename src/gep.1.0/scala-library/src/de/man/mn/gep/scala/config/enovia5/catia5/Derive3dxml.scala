package de.man.mn.gep.scala.config.enovia5.catia5

import java.io.PrintWriter

import scala.collection.immutable.Map

import org.restlet.data.ChallengeResponse

import com.ibm.de.ebs.plm.scala.json.JsonConversions.Any2Json
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2Object
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.text.StringConversions.fromHexString
import com.ibm.de.ebs.plm.scala.util.Io.addZipEntry

class Derive3dxml(parameters: Map[String, String], authorization: ChallengeResponse) extends Derive(parameters, authorization) {

  val mimetype = "cgr"

  val innerextension = "cgr"

  val assemblyfile = "$.3dxml"

  def ignore(filename: String) = "Manifest.xml" == filename

  val withUnusedReferences = true

  def writeManifest = {
    addZipEntry("Manifest.xml") {
      writer.print(manifest)
      writer.flush
    }
    zipout.flush
  }

  def writeAssemblyHeader = {
    writer.print(header)
  }

  def writeAssemblyBody = {
    var i = 0
    versions.foreach { reference =>
      i += 1
      writeReference(writer, reference)
    }
    instances.foreach { instance =>
      i += 3
      writeInstance(writer, instance, i)
    }
    if (withUnusedReferences) {
      val k = i + 3
      unusedreferences.foreach { unusedreference =>
        i += 3
        writeUnreferenced(writer, unusedreference, i, k)
      }
    }
  }

  def writeAssemblyFooter = {
    writer.print(footer)
  }

  def writeInstance(writer: PrintWriter, instance: Json.JObject, i: Int) = {
    val aggregatedby = instance.get("aggregatedby").asInt
    if (0 < aggregatedby) {
      val instanceof = instance.get("instanceof").asInt
      writer.print(instance1)
      writer.print(prefix + i)
      writer.print(instance2)
      writer.print(instance3)
      writer.print(prefix + aggregatedby)
      writer.print(instance4)
      writer.print(prefix + instanceof)
      writer.print(instance5)
      writer.print(double(instance.get("m1")))
      writer.print(" "); writer.print(double(instance.get("m2")))
      writer.print(" "); writer.print(double(instance.get("m3")))
      writer.print(" "); writer.print(double(instance.get("m4")))
      writer.print(" "); writer.print(double(instance.get("m5")))
      writer.print(" "); writer.print(double(instance.get("m6")))
      writer.print(" "); writer.print(double(instance.get("m7")))
      writer.print(" "); writer.print(double(instance.get("m8")))
      writer.print(" "); writer.print(double(instance.get("m9")))
      writer.print(" "); writer.print(double(instance.get("m10"), true))
      writer.print(" "); writer.print(double(instance.get("m11"), true))
      writer.print(" "); writer.print(double(instance.get("m12"), true))
      writer.print(instance6)
      if (withUnusedReferences) {
        if (geometries.contains(instanceof) && !geometriesprocessesd.contains(instanceof)) {
          writer.print(instancerep1)
          writer.print(prefix + (i + 1))
          writer.print(instance2)
          writer.print(instance3)
          writer.print(prefix + instanceof)
          writer.print(instance4)
          writer.print(prefix + (i + 2))
          writer.print(instancerep2)
          writer.print(referencerep1)
          writer.print(prefix + (i + 2))
          writer.print(referencerep2)
          writer.print(unmakeprefix(prefix) + geometries.getOrElse(instanceof, ""))
          writer.print(referencerep3)
          geometriesprocessesd.add(instanceof)
        }
      }
    }
  }

  def writeReference(writer: PrintWriter, reference: Json.JObject) = {
    val id = reference.get("id").asInt
    val displayname = reference.get("displayname").asString
    val filename = reference.get("filename").asString + "." + innerextension
    val isassembly = reference.get("isassembly").asBoolean
    val attributes = reference.get("attributes").asObject

    writer.print(reference1)
    writer.print(prefix + id)
    writer.print(reference2)
    writer.print(escape(displayname))

    val partnotloaded = !isassembly && !files.contains(filename)
    val withgeometry = !isassembly && files.contains(filename)

    if (partnotloaded) {
      writer.print("  -  N/A")
      unusedreferences.add(id)
    } else if (withgeometry) {
      geometries.put(id, filename)
    }
    writer.print(reference3)
    writer.print("""
    		  <V_filename>""")
    writer.print(escape(filename))
    writer.print("</V_filename>")
    attributes.foreach {
      case (n, v) => if (0 < v.toString.length) writer.print("""
    		  <""" + n + ">" + escape(v.toString) + "</" + n + ">")
    }
    writer.print(reference4)
  }

  def writeUnreferenced(writer: PrintWriter, id: Int, i: Int, k: Int) = {
    try {
      if (k == i) {
        writer.print(reference1)
        writer.print(prefix + i)
        writer.print(reference2)
        writer.print("Geometry not available")
        writer.print(reference3)
        writer.print(reference4)
        writer.print(instance1)
        writer.print(prefix + (i + 1))
        writer.print(instance2)
        writer.print("Network or file creation problem")
        writer.print(instance3)
        writer.print(prefix + 1)
        writer.print(instance4)
        writer.print(prefix + k)
        writer.print(instance8)
      }
      writer.print(instance1)
      writer.print(prefix + (i + 2))
      writer.print(instance2)
      writer.print(instance3)
      writer.print(prefix + k)
      writer.print(instance4)
      writer.print(prefix + id)
      writer.print(instance8)
    } catch { case e => e.printStackTrace }
  }

  override def writeBasketAssemblyBodyHeader = {
    writer.print(reference3D.format(fromHexString(parameters("nicename"))))
    var i = 0
    entries.toList.foreach { entry =>
      i += 1
      writer.print(instance3D.format(i + 1, makeprefix(i) + 1))
    }
  }

  def double(json: Option[Json], scale: Boolean = false): Double = {
    val epsilon = 1e-15
    json match {
      case Some(d) => (if (epsilon > scala.math.abs(d.asDouble)) 0. else d.asDouble) * (if (scale) 1000. else 1.)
      case _ => 0.
    }
  }

  def escape(s: String) = s.replace("&", "&amp;").replace(">", "&gt;").replace("<", "&lt;").replace("\"", "&quot;").replace("'", "&apos;")

  val geometries = new scala.collection.mutable.HashMap[Int, String]

  val unusedreferences = new scala.collection.mutable.BitSet

  val geometriesprocessesd = new scala.collection.mutable.BitSet

  private val manifest = """<?xml version="1.0" encoding="utf-8" ?>
<Manifest xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="Manifest.xsd">
	<Root>$.3dxml</Root>
	<WithAuthoringData>false</WithAuthoringData>
</Manifest>
"""

  private val header = """<?xml version="1.0" encoding="utf-8" ?>
<Model_3dxml>
	<Header>
		<SchemaVersion>4.3</SchemaVersion>
        <Generator>GEPserver/1.0</Generator>
	</Header>
	<ProductStructure root="1">"""

  private val reference1 = """
		<Reference3D id=""""

  private val reference2 = """" name=""""

  private val reference3 = """">"""

  private val reference4 = """
        </Reference3D>"""

  private val instance1 = """
		<Instance3D id=""""

  private val instance2 = """" name=""""

  private val instance3 = """">
			<IsAggregatedBy>"""

  private val instance4 = """</IsAggregatedBy>
			<IsInstanceOf>"""

  private val instance5 = """</IsInstanceOf>
			<RelativeMatrix>"""

  private val instance6 = """</RelativeMatrix>
		</Instance3D>"""

  private val instance7 = """</IsInstanceOf>
		</Instance3D>"""

  private val instance8 = """</IsInstanceOf>
        	<RelativeMatrix>1 0 0 0 1 0 0 0 1 0 0 0</RelativeMatrix>
		</Instance3D>"""

  private val instancerep1 = """
		<InstanceRep id=""""

  private val instancerep2 = """</IsInstanceOf>
		</InstanceRep>"""

  private val referencerep1 = """
		<ReferenceRep id=""""

  private val referencerep2 = """" name="_ReferenceRep" format="TESSELLATED" version="2.2" associatedFile="urn:3DXML:"""

  private val referencerep3 = """"/>"""

  private val footer = """
	</ProductStructure>
	<DefaultSessionProperties>
    	<ViewpointStyle>PARALLEL</ViewpointStyle>
    	<RenderingStyle>SHADING_WITH_EDGES_WITHOUT_SMOOTH_EDGES</RenderingStyle>
	</DefaultSessionProperties>
</Model_3dxml>
"""

  private val reference3D = """
		<Reference3D id="1" name="%1$s"/>"""

  private val instance3D = """
		<Instance3D id="%1$s" name="">
			<IsAggregatedBy>1</IsAggregatedBy>
			<IsInstanceOf>%2$s</IsInstanceOf>
			<RelativeMatrix>1 0 0 0 1 0 0 0 1 0 0 0</RelativeMatrix>
		</Instance3D>"""

}
