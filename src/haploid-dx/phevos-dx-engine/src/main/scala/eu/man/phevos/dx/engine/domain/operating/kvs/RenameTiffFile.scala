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
import eu.man.phevos.dx.kvs.services.GetKVSFilenameForTIFFInput
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.util.interfaces.{PartInfo, MTBPartFile}
import javax.xml.bind.annotation.{XmlType, XmlRootElement}

@XmlRootElement(name = "rename-tiff-operation-detail")
case class RenameTiffFileOperationDetail(

  @xmlElement(required = true) partinfo: PartInfo)

  extends OperationDetail("rename-tiff") {

  def this() = this(null)

}

class RenameTiffFileOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local

  with FileHandler {

  type PreProcessingInput = MTBPartFile

  type ProcessingInput = MTBPartFile

  type PostProcessingInput = MTBPartFile

  type PostProcessingOutput = MTBPartFile

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] = {
    copyFileToWorkingDirectory(input.path) match {
      case Success(path) ⇒
        Success(MTBPartFile(path))
    }
  }

  protected[this] def doProcessing(input: ProcessingInput): Result[PostProcessingInput] = {
    operation.detail match {
      case RenameTiffFileOperationDetail(partinfo) ⇒
        KVSServices.GetKVSFilenameForTIFF(GetKVSFilenameForTIFFInput(partinfo, input.metadata)) match {
          case Success(filename) ⇒
            renameFile(input.path, filename) match {
              case Success(file) ⇒
                Success(MTBPartFile(file))
            }
        }
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    moveFileToOutputDirectory(input.path) match {
      case Success(file) ⇒
        Success(MTBPartFile(file))
    }
  }

}
