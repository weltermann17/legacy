package eu.man.phevos

package dx

package engine

package domain

package operating

package catia5

import java.nio.file.{ Paths, Path }

import javax.xml.bind.annotation.XmlType

import scala.collection.JavaConversions._

import akka.event.LoggingAdapter

import com.ibm.haploid.core.service.{ Success, Result }
import com.ibm.haploid.core.util.text.fromBase64String
import com.ibm.haploid.core.machinename
import com.ibm.haploid.dx.engine.domain.operating.{ ExternalOperator, ExternalOperationDetail, consolecharset }
import com.ibm.haploid.dx.engine.event.OperationCreate
import java.nio.file.Paths
import java.util.jar.JarFile
import java.io.{ OutputStream, InputStream, FileOutputStream, File }
import scala.collection.JavaConversions._
import javax.xml.bind.annotation.XmlRootElement
import eu.man.phevos.dx.util.interfaces.FileWithPath

/**
 *
 */
@XmlRootElement(name = "createdmufrompart-catscript-operation-detail")
case class CreateDMUFromPartCATScriptOperationDetail()
  extends ExternalOperationDetail(
    "createdmufrompart",
    "phevos_create_dmu_from_part.CATScript",
    "Create_DMU_From_Part",
    List(),
    Map("CNEXTOUTPUT" -> "cnextlog.txt"),
    "cnextlog.txt", 60 * 60 * 1000)

case class DMUInput(@transient _partfile: Path, newpartname: String)

  extends FileWithPath(_partfile) {

  def partfile = path

}

case class DMUOutput(@transient _partfile: Path)

  extends FileWithPath(_partfile) {

  def partfile = path

}

/**
 *
 */
case class CreateDMUFromPartCATScriptOperator(

  operation: OperationCreate,

  basedirectory: Path,

  log: LoggingAdapter,

  timeout: Long)

  extends RunCATScriptOperator {

  type PreProcessingInput = DMUInput

  type ProcessingInput = DMUOutput

  type PostProcessingInput = DMUOutput

  type PostProcessingOutput = DMUOutput

  override protected[this] def processReturnCode(returncode: Int): Int = super.processReturnCode(returncode) match {
    case 0 ⇒ if (fromBase64String(getLogfile, consolecharset).contains("Finished successfully")) 0 else -1
    case c ⇒ c
  }

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] = {
    copyFileToWorkingDirectory(input.partfile) match {
      case Success(path) ⇒ {
        addEnvironmentVariable("PHEVOS_DX_SRC_FILE", path.toAbsolutePath.toString)
        addEnvironmentVariable("PHEVOS_DX_SRC_FOLDER", workingdirectory.toAbsolutePath.toString + "\\")
        addEnvironmentVariable("PHEVOS_DX_NEW_PART_NAME", input.newpartname)

        Success(DMUOutput(Paths.get(workingdirectory.toAbsolutePath.toString + "\\" + input.newpartname + ".CATPart")))
      }
    }
  }

  protected[this] def doProcessing(input: ProcessingInput) = Success(DMUOutput(input.partfile.toAbsolutePath))

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    moveFileToOutputDirectory(input.partfile) match {
      case Success(partfile) ⇒
        Success(DMUOutput(partfile))
    }
  }

}

