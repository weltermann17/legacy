package eu.man.phevos

package dx

package engine

package domain

package operating

package util

import java.io.{ FileOutputStream, FileInputStream, File, BufferedOutputStream, BufferedInputStream }
import java.nio.file.{ Paths, Path, Files }
import java.text.SimpleDateFormat
import java.util.Date
import javax.xml.bind.annotation.XmlType
import org.apache.commons.compress.archivers.tar.{ TarArchiveOutputStream, TarArchiveEntry }
import akka.event.LoggingAdapter
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local, FileHandler }
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.event.OperationCreate
import javax.xml.bind.annotation.XmlRootElement
import com.ibm.haploid.core.service.SimpleServiceException

/**
 *
 */
@XmlRootElement(name = "create-tar-operation-detail")
case class CreateTarOperationDetail(

  @xmlAttribute(required = true) usecompression: Boolean)

  extends OperationDetail("createtar") {

  def this() = this(false)

}

case class TarInput(@transient sourcefile: Path, tarname: String, kvsuserident: String)
case class TarFile(@transient tarfile: Path)

/**
 *
 */
case class CreateTarOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local

  with FileHandler {

  type PreProcessingInput = TarInput

  type ProcessingInput = TarInput

  type PostProcessingInput = TarFile

  type PostProcessingOutput = TarFile

  protected[this] def doPreProcessing(input: PreProcessingInput) = {
    copyFileToWorkingDirectory(input.sourcefile) match {
      case Success(path) ⇒
        val tarname = {
          if (input.tarname.contains(".tar")) {
            input.tarname
          } else {
            input.tarname + ".tar"
          }
        }
        Success(TarInput(path, tarname, input.kvsuserident))
    }
  }

  protected[this] def doProcessing(input: ProcessingInput) = {
    generatePhevosTar(
      input.sourcefile,
      input.tarname,
      input.kvsuserident) match {
        case tarfile: Path ⇒
          Success(TarFile(tarfile))
      }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    moveFileToOutputDirectory(input.tarfile) match {
      case Success(tar) ⇒
        Success(TarFile(tar))
    }
  }

  /**
   * Generates a Phevos TAR file with KVS header file
   * @param workDir Working directory as String
   */
  private def generatePhevosTar(sourcefile: Path, tarname: String, kvsuserident: String): Path = {

    val tarfile = Paths.get(workingdirectory.toAbsolutePath + "\\" + tarname)

    val fileToHeader = createPhevosHeaderFile(sourcefile, kvsuserident)
    val contentOfHeader = {
      val inputStream: BufferedInputStream = new BufferedInputStream(new FileInputStream(fileToHeader))
      var byteData: Array[Byte] = Stream.continually(inputStream.read).takeWhile(-1 !=).map(_.toByte).toArray
      inputStream.close
      byteData
    }

    val fileToAdd = new File(sourcefile.toUri)
    val contentOfEntry = {
      val inputStream: BufferedInputStream = new BufferedInputStream(new FileInputStream(fileToAdd))
      var byteData: Array[Byte] = Stream.continually(inputStream.read).takeWhile(-1 !=).map(_.toByte).toArray
      inputStream.close
      byteData
    }

    val tarOutput: TarArchiveOutputStream = new TarArchiveOutputStream(new BufferedOutputStream(
      new FileOutputStream(tarfile.toFile)))

    val header: TarArchiveEntry = new TarArchiveEntry(fileToHeader, fileToHeader.getName)
    tarOutput.putArchiveEntry(header)
    tarOutput.write(contentOfHeader, 0, contentOfHeader.length)
    tarOutput.closeArchiveEntry

    val entry: TarArchiveEntry = new TarArchiveEntry(fileToAdd, sourcefile.getFileName.toString)
    tarOutput.putArchiveEntry(entry)
    tarOutput.write(contentOfEntry, 0, contentOfEntry.length)
    tarOutput.closeArchiveEntry

    tarOutput.close

    tarfile
  }

  /**
   * Generate the Phevos TAR header file
   * @param workDir Working directory as String
   */
  private def createPhevosHeaderFile(sourcefile: Path, kvsuserident: String): File = {

    val fileExtension = {
      val resultStrings = sourcefile.getFileName.toString.split("\\.")
      resultStrings(resultStrings.length - 1)
    }
    val headerFileType = fileExtension match {
      case "CATPart" ⇒ "CATPart TAR"
      case "CATProduct" ⇒ "CATProduct TAR"
      case "CATDrawing" ⇒ "CATDrawing TAR"
      case _ ⇒ throw SimpleServiceException("Unsupported file extension")
    }

    val creationDate = {
      val today = new Date(System.currentTimeMillis)
      val headerFormat = new SimpleDateFormat("yyyyMMddhhmmss")
      headerFormat.format(today)
    }

    val headerFileName = "=" + headerFileType + "=CATIA V5=V5R19=" + creationDate + "=PHEVOS_DX=hyperkvs@" + kvsuserident + "="

    val headerFile = new File(workingdirectory.toAbsolutePath + "\\" + headerFileName)
    val headerFileoutputStream: BufferedOutputStream = new BufferedOutputStream(new FileOutputStream(headerFile))
    headerFileoutputStream.write(sourcefile.getFileName.toString.getBytes)
    headerFileoutputStream.close

    headerFile
  }
}
