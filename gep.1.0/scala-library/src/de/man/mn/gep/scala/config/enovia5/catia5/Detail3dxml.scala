package de.man.mn.gep.scala.config.enovia5.catia5

import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.zip.Deflater
import java.util.zip.ZipOutputStream

import scala.collection.immutable.Map

import org.restlet.data.MediaType
import org.restlet.representation.Representation
import org.restlet.resource.Get
import org.restlet.resource.ServerResource

import com.ibm.de.ebs.plm.scala.resource.c
import com.ibm.de.ebs.plm.scala.resource.disposable
import com.ibm.de.ebs.plm.scala.resource.forceContextType
import com.ibm.de.ebs.plm.scala.resource.using
import com.ibm.de.ebs.plm.scala.rest.WritableByteChannelRepresentation
import com.ibm.de.ebs.plm.scala.text.StringConversions.fromHexString
import com.ibm.de.ebs.plm.scala.util.Io.addDisposition
import com.ibm.de.ebs.plm.scala.util.Io.addZipEntry

case class Detail3dxml(
  resource: ServerResource,
  parameters: Map[String, String])
  extends ServerResource {

  override def doInit = {
    resource.setRequest(getRequest)
    resource.setResponse(getResponse)
  }

  @Get
  def doRequest = {
    getRequest.getClientInfo.getAcceptedEncodings.clear
    resource.handle
    val request = getRequest
    val response = getResponse
    if (response.getStatus.isSuccess) {
      response.setEntity(PartRepresentation(response.getEntity))
    }
    addDisposition(response.getEntity, parameters, "nativeformat")
  }

  private case class PartRepresentation(representation: Representation)
    extends WritableByteChannelRepresentation(MediaType.APPLICATION_ZIP) {

    override def write(out: java.io.OutputStream) = {
      using {
        implicit val _ = forceContextType[Unit]
        implicit val zipout = disposable(new ZipOutputStream(out))
        val writer = new PrintWriter(new OutputStreamWriter(zipout, "UTF-8"))
        zipout.setLevel(Deflater.DEFAULT_COMPRESSION)
        zipout.setComment(comment)
        addZipEntry("Manifest.xml") { writeManifest(writer) }
        addZipEntry("$.cgr") { representation.write(zipout) }
        addZipEntry("$.3dxml") { write3dxml(writer) }
      }
    }

    def write3dxml(writer: java.io.PrintWriter) = {
      writer.print(header1)
      writer.print(name)
      writer.print(header2)
      writer.print(footer)
      writer.flush
    }

    def writeManifest(writer: java.io.PrintWriter) = {
      writer.print(manifest)
      writer.flush
    }
  }

  private val name = {
    val n = fromHexString(parameters("nicename"))
    n.substring(0, n.indexOf(".3dxml"))
  }

  private val comment = "MAN Truck & Bus AG - generated with GEPserver/1.0"

  private val header1 = """<?xml version="1.0" encoding="utf-8" ?>
<Model_3dxml>
	<Header>
		<SchemaVersion>4.3</SchemaVersion>
        <Generator>GEPserver/1.0</Generator>
	</Header>
	<ProductStructure root="1">
		<Reference3D id="1" name=""""

  private val header2 = """"/>
		<ReferenceRep id="2" name="_ReferenceRep" format="TESSELLATED" version="2.2" associatedFile="urn:3DXML:$.cgr"/>
		<InstanceRep id="3" name="_InstanceRep">
			<IsAggregatedBy>1</IsAggregatedBy>
			<IsInstanceOf>2</IsInstanceOf>
		</InstanceRep>
	</ProductStructure>"""

  private val footer = """
</Model_3dxml>
"""

  private val manifest = """<?xml version="1.0" encoding="utf-8" ?>
<Manifest xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="Manifest.xsd">
	<Root>$.3dxml</Root>
	<WithAuthoringData>false</WithAuthoringData>
</Manifest>
"""

}
