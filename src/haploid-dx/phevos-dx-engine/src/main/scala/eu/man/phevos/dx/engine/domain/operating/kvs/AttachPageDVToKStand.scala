package eu.man.phevos.dx.engine.domain.operating.kvs

import java.nio.file.Path

import com.ibm.haploid.core.service.{Success, Result}
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.operating.{OperatorBase, OperationDetail, Local}
import com.ibm.haploid.dx.engine.event.OperationCreate

import akka.event.LoggingAdapter
import eu.man.phevos.dx.kvs.services.AttachPageDVToKStandInput
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.util.interfaces.{PartInfo, EZISMetadata}
import javax.xml.bind.annotation.{XmlType, XmlRootElement}

@XmlRootElement(name = "attach-page-dv-to-kstand-operation-detail")
case class AttachPageDVToKStandOperationDetail(

  @xmlElement(required = true) partinfo: PartInfo)

  extends OperationDetail("attach-page-dv-to-kstand") {

  def this() = this(null)

}

class AttachPageDVToKStandOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local {

  type PreProcessingInput = EZISMetadata

  type ProcessingInput = EZISMetadata

  type PostProcessingInput = Boolean

  type PostProcessingOutput = Boolean

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] = Success(input)

  protected[this] def doProcessing(input: ProcessingInput): Result[PostProcessingInput] = {
    operation.detail match {
      case AttachPageDVToKStandOperationDetail(partinfo) ⇒
        KVSServices.AttachPageDVToKStand(AttachPageDVToKStandInput(partinfo, input))
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput] = Success(input)

}
