package eu.man.phevos

package dx

package engine

package domain

package operating

package gep

import java.nio.file.{ Path, Paths }
import javax.xml.bind.annotation.XmlType
import akka.event.LoggingAdapter
import com.ibm.haploid.core.service.{ Success, Failure, Result }
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local, FileHandler }
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.event.OperationCreate
import dx.gep.{ GEPServices, DownloadCATPartInput, CATPartFileResult }
import javax.xml.bind.annotation.XmlRootElement

/**
 *
 */
@XmlRootElement(name = "getcatpart-operation-detail")
case class GetCATPartOperationDetail(

  @xmlElement(required = true) partnumber: String,

  @xmlElement(required = true) partindex: String)

  extends OperationDetail("getcatpart") {

  def this() = this(null, null)

}

/**
 *
 */
class GetCATPartOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local

  with FileHandler {

  type PreProcessingInput = String

  type ProcessingInput = String

  type PostProcessingInput = CATPartFileResult

  type PostProcessingOutput = CATPartFileResult

  protected[this] def doPreProcessing(input: PreProcessingInput) = Success(input.asInstanceOf[String])

  protected[this] def doProcessing(input: ProcessingInput) = {
    operation.detail match {
      case GetCATPartOperationDetail(partnumber, partindex) ⇒ {
        val result = GEPServices.DownloadFile(DownloadCATPartInput(partnumber, partindex, Paths.get(workingdirectory.toAbsolutePath.toString + "\\" + input)))
        result match {
          case Success(result) ⇒
            Success(result.asInstanceOf[CATPartFileResult])
        }
      }
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    moveFileToOutputDirectory(input.path) match {
      case Success(catpart) ⇒ {
        Success(CATPartFileResult(catpart))
      }
    }
  }

}

