package eu.man.phevos

package dx

package engine

package domain

package operating

package gep

import java.nio.file.{ Path }
import javax.xml.bind.annotation.XmlType
import akka.event.LoggingAdapter
import com.ibm.haploid.core.service.{ Success, Failure }
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local, FileHandler }
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.event.OperationCreate
import dx.gep.{ GEPServices }
import dx.gep._
import eu.man.phevos.dx.gep.PartMetadataInput
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "getpartmetadata-operation-detail")
case class GetPartMetadataOperationDetail(

  @xmlElement(required = true) partinput: PartMetadataInput)

  extends OperationDetail("get-part-meta-data") {

  def this() = this(null)

}

class GetPartMetadataOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase {

  type PreProcessingInput = Unit

  type ProcessingInput = Unit

  type PostProcessingInput = PartMetadata

  type PostProcessingOutput = PartMetadata

  protected[this] def doPreProcessing(input: PreProcessingInput) = {
    Success(())
  }

  protected[this] def doProcessing(input: ProcessingInput) = {
    operation.detail match {
      case GetPartMetadataOperationDetail(partinput) ⇒
        GEPServices.GetMetadata(partinput) match {
          case Success(partinput) ⇒
            Success(partinput)
          case Failure(reason) ⇒ Failure(reason)
        }
    }

  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = Success(input)

}

