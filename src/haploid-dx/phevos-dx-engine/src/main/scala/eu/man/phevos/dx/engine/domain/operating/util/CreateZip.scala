package eu.man.phevos

package dx

package engine

package domain

package operating

package util

import java.io.{ FileOutputStream, FileInputStream, BufferedOutputStream, BufferedInputStream }
import java.nio.file.{ Path, Files }
import java.util.zip.{ ZipOutputStream, ZipEntry }

import javax.xml.bind.annotation.XmlType

import scala.collection.JavaConversions.iterableAsScalaIterable

import akka.event.LoggingAdapter

import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.util.io.copyBytes
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local, FileHandler }
import com.ibm.haploid.dx.engine.event.OperationCreate
import java.util.zip.ZipOutputStream
import collection.JavaConversions._
import java.util.zip.ZipEntry
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "create-zip-operation-detail")
case class CreateZipOperationDetail(

  @xmlAttribute(required = true) usecompression: Boolean)

  extends OperationDetail("createzip") {

  def this() = this(false)

}

case class ZipInput(@transient sourcefile: Path, zipname: String)
case class ZipFile(@transient zipfile: Path)

/**
 *
 */
case class CreateZipOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local

  with FileHandler {

  type PreProcessingInput = ZipInput

  type ProcessingInput = ZipInput

  type PostProcessingInput = ZipFile

  type PostProcessingOutput = ZipFile

  protected[this] def doPreProcessing(input: PreProcessingInput) = {
    val zipname = if (input.zipname.endsWith(".zip")) input.zipname else input.zipname + ".zip"
    Success(ZipInput(input.sourcefile, zipname))
  }

  protected[this] def doProcessing(input: ProcessingInput) = {
    zipDirectory(
      input.sourcefile,
      input.zipname) match {
        case zipfile: Path ⇒
          Success(ZipFile(zipfile))
      }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    moveFileToOutputDirectory(input.zipfile) match {
      case Success(zip) ⇒
        Success(ZipFile(zip))
    }
  }

  /**
   * Zip all files in the source directory into a zipfile with the given name.
   */
  private def zipDirectory(sourcedirectory: Path, zipname: String): Path = {
    val zipfile = workingdirectory.resolve(zipname)
    val zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipfile.toFile), 2 * 1024))
    val inputfiles = Files.newDirectoryStream(sourcedirectory)
    try {
      inputfiles.foreach { file ⇒
        zip.putNextEntry(new ZipEntry(file.getFileName.toString))
        val in = new BufferedInputStream(new FileInputStream(file.toFile), 2 * 1024)
        try {
          copyBytes(in, zip)
        } finally {
          in.close
          zip.closeEntry
        }
      }
    } finally {
      inputfiles.close
      zip.close
    }
    zipfile
  }

}
