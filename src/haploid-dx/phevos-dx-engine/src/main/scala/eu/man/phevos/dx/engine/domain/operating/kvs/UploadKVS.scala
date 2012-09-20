package eu.man.phevos

package dx

package engine

package domain

package operating

package kvs

import java.nio.file.Path

import com.ibm.haploid.core.service.{Success, Result}
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.operating.{OperatorBase, OperationDetail, Local, FileHandler}
import com.ibm.haploid.dx.engine.event.OperationCreate

import akka.event.LoggingAdapter
import dx.kvs.services.UploadFileInput
import dx.kvs.KVSServices
import eu.man.phevos.dx.util.interfaces.{PartInfo, MTBPartFile}
import javax.xml.bind.annotation.{XmlType, XmlRootElement}

/**
 * Input data
 */
@XmlRootElement(name = "uploadkvs-operation-detail")
case class UploadKVSOperationDetail(

  @xmlElement(required = true) partinfo: PartInfo)

  extends OperationDetail("uploadkvs") {

  def this() = this(null)

}

class UploadKVSOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local

  with FileHandler {

  type PreProcessingInput = MTBPartFile

  type ProcessingInput = MTBPartFile

  type PostProcessingInput = Boolean

  type PostProcessingOutput = Boolean

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] = {
    copyFileToWorkingDirectory(input.path) match {
      case Success(path) ⇒
        Success(MTBPartFile(path))
    }
  }

  protected[this] def doProcessing(input: ProcessingInput): Result[PostProcessingInput] = {
    operation.detail match {
      case UploadKVSOperationDetail(partinfo) ⇒
        KVSServices.UploadFile(UploadFileInput(partinfo, input))
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput] = Success(input)

}