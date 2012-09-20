package eu.man.phevos.dx.engine.domain.operating.kvs

import java.nio.file.Path

import com.ibm.haploid.core.service.{Success, Result}
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.operating.{OperatorBase, OperationDetail, Local}
import com.ibm.haploid.dx.engine.event.OperationCreate

import akka.event.LoggingAdapter
import eu.man.phevos.dx.kvs.services.CheckDVForEZISFileExistsInput
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.util.interfaces.{PartInfo, EZISMetadata}
import javax.xml.bind.annotation.{XmlType, XmlRootElement}

@XmlType(name = "check-ezis-file-dv-exists-operation-detail")
case class CheckDVForEZISFileExistsOperationDetail(

  @xmlElement(required = true) vwPartInfo: PartInfo)

  extends OperationDetail("check-ezis-file-dv-exists") {

  def this() = this(null)

}

@XmlRootElement(name = "check-ezis-file-dv-exists-operation-output")
case class CheckDVForEZISFileExistsOutput(

  exists: Boolean,

  vwPartInfo: EZISMetadata) {

  def this() = this(false, null)

}

class CheckDVForEZISFileExistsOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local {

  type PreProcessingInput = EZISMetadata

  type ProcessingInput = EZISMetadata

  type PostProcessingInput = CheckDVForEZISFileExistsOutput

  type PostProcessingOutput = CheckDVForEZISFileExistsOutput

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] = Success(input)

  protected[this] def doProcessing(input: ProcessingInput): Result[PostProcessingInput] = {
    operation.detail match {
      case CheckDVForEZISFileExistsOperationDetail(partInfo) ⇒
        KVSServices.CheckDVForEZISFileExists(CheckDVForEZISFileExistsInput(partInfo, input)) match {
          case (Success(b)) ⇒
            Success(CheckDVForEZISFileExistsOutput(b, input))
        }
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput] = Success(input)

}