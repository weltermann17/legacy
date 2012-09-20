package eu.man.phevos

package dx

package engine

package domain

package operating

package crt

import java.nio.file.Path
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local }
import com.ibm.haploid.dx.engine.event.OperationCreate
import akka.event.LoggingAdapter
import dx.crt.CrtServices.{ EntitledParts, EntitledDxStatus }
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "entitleddxstatus-operation-detail")
case class EntitledDxStatusOperationDetail(iDontCare: Boolean)

  extends OperationDetail("entitleddxstatus") {

  def this() = this(false)
}

class EntitledDxStatusOperator(
  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase with Local {

  type PreProcessingInput = Unit

  type ProcessingInput = Unit

  type PostProcessingInput = EntitledParts

  type PostProcessingOutput = EntitledParts

  protected[this] def doPreProcessing(input: PreProcessingInput) = {
    Success({})
  }

  protected[this] def doProcessing(input: ProcessingInput) = {
    operation.detail match {
      case EntitledDxStatusOperationDetail(dummy) ⇒ EntitledDxStatus({})
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    Success(input)
  }

}