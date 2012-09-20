package eu.man.phevos

package dx

package engine

package domain

package operating

package catia5

import java.nio.file.Path
import java.sql.Timestamp
import java.text.SimpleDateFormat

import com.ibm.haploid.core.service.{Success, Result}
import com.ibm.haploid.core.util.text.fromBase64String
import com.ibm.haploid.dx.engine.domain.operating.consolecharset
import com.ibm.haploid.dx.engine.domain.operating.ExternalOperationDetail
import com.ibm.haploid.dx.engine.event.OperationCreate

import akka.event.LoggingAdapter
import eu.man.phevos.dx.util.interfaces.MTBPartFile
import javax.xml.bind.annotation.XmlRootElement

/**
 *
 */
@XmlRootElement(name = "reframetiff-catscript-operation-detail")
case class ReframeTiffCATScriptOperationDetail(

  val mtbPartNumber: String,

  val mtbPartIndex: String,

  val vwPartNumber: String)

  extends ExternalOperationDetail(
    "reframetiff",
    "phevos_prepare_tif_files.CATScript",
    "Reframe_Tiff",
    List(
      "OPFILE.1",
      "Publish.CATSettings",
      "A0_TEMPLATE.CATDrawing",
      "A1_TEMPLATE.CATDrawing",
      "A2_TEMPLATE.CATDrawing",
      "A3_TEMPLATE.CATDrawing",
      "A4_PORTRAIT_TEMPLATE.CATDrawing",
      "B0_TEMPLATE.CATDrawing",
      "B1_TEMPLATE.CATDrawing",
      "B2_TEMPLATE.CATDrawing",
      "B3_TEMPLATE.CATDrawing",
      "C0_TEMPLATE.CATDrawing"),

    Map("CNEXTOUTPUT" -> "cnextlog.txt"), "cnextlog.txt", 60 * 60 * 1000) {

  private def this() = this(null, null, null)

}

case class ReframeTiffCATScriptOperationInput(

  val file: MTBPartFile,

  val pgPartName: String,

  val egPartName: String,

  val mtbDrawingdate: Timestamp,

  val mlChangeNumber: String)

/**
 *
 */
case class ReframeTiffCATScriptOperator(

  operation: OperationCreate,

  basedirectory: Path,

  log: LoggingAdapter,

  timeout: Long)

  extends RunCATScriptOperator {

  type PreProcessingInput = ReframeTiffCATScriptOperationInput

  type ProcessingInput = MTBPartFile

  type PostProcessingInput = MTBPartFile

  type PostProcessingOutput = MTBPartFile

  override protected[this] def processReturnCode(returncode: Int): Int = super.processReturnCode(returncode) match {
    case 0 ⇒ if (fromBase64String(getLogfile, consolecharset).contains("Finished successfully")) 0 else -1
    case c ⇒ c
  }

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] = {
    operation.detail match {
      case details: ReframeTiffCATScriptOperationDetail ⇒
        addEnvironmentVariable("PHEVOS_DX_USER_WORK_DIR", workingdirectory.toAbsolutePath.toString)
        addEnvironmentVariable("PHEVOS_DX_MTB_PART_NUMBER", details.mtbPartNumber)
        addEnvironmentVariable("PHEVOS_DX_MTB_INDEX", details.mtbPartIndex)
        addEnvironmentVariable("PHEVOS_DX_MTB_DRAWING_DATE", new SimpleDateFormat("dd.MM.yy").format(input.mtbDrawingdate))
        addEnvironmentVariable("PHEVOS_DX_ML_CHANGE_NUMBER", input.mlChangeNumber)
        addEnvironmentVariable("PHEVOS_DX_VW_PART_NUMBER", details.vwPartNumber)
        addEnvironmentVariable("PHEVOS_DX_ML_PT_NAME", input.pgPartName)
        addEnvironmentVariable("PHEVOS_DX_ML_EN_NAME", input.egPartName)

        copyFileToWorkingDirectory(input.file.path) match {
          case Success(path) ⇒ Success(MTBPartFile(path))
        }
    }
  }

  protected[this] def doProcessing(input: ProcessingInput) = { Success(input) }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {

    moveFileToOutputDirectory(input.path) match {
      case Success(tifffile) ⇒ Success(MTBPartFile(tifffile))
    }

  }

}

