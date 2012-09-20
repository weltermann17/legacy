package eu.man.phevos

package dx

package engine

package domain

package operating

package crt

import java.nio.file.Path
import javax.xml.bind.annotation.XmlType
import akka.event.LoggingAdapter
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local, FileHandler }
import com.ibm.haploid.dx.engine.event.OperationCreate
import com.ibm.haploid.dx.engine.domain.binding._
import dx.crt.CrtServices.{ Identifier, DxStatus, CurrentDxStatus }
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "dxstatus-operation-detail")
case class DxStatusOperationDetail(

  @xmlAttribute(required = true) dummy: String)

  extends OperationDetail("dxstatus") {

  def this() = this(null)
}

class DxStatusOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local {

  type PreProcessingInput = Identifier

  type ProcessingInput = Identifier

  type PostProcessingInput = CurrentDxStatus

  type PostProcessingOutput = CurrentDxStatus

  protected[this] def doPreProcessing(input: PreProcessingInput) = {
    Success(input)
  }

  protected[this] def doProcessing(input: ProcessingInput) = {
    operation.detail match {

      case DxStatusOperationDetail(dummy) ⇒ DxStatus(input)

    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    Success(input)
  }

}