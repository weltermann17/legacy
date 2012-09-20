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
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local }
import com.ibm.haploid.dx.engine.event.OperationCreate
import com.ibm.haploid.dx.engine.domain.binding._
import dx.crt.CrtServices.{ SetDxStatusToWork, Identifier, CurrentDxStatus }
import eu.man.phevos.dx.util.interfaces.PartInfo
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "setdxstatus-operation-detail")
case class SetDxStatusOperationDetail(

  @xmlElement(required = true) partinfo: PartInfo)

  extends OperationDetail("setdxstatus2work") {

  def this() = this(null)
}

class SetDxStatusOperator(
  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase 
  
  with Local {

  type PreProcessingInput = Unit

  type ProcessingInput = Unit

  type PostProcessingInput = CurrentDxStatus

  type PostProcessingOutput = CurrentDxStatus

  protected[this] def doPreProcessing(input: PreProcessingInput) = {
    Success(input)
  }

  protected[this] def doProcessing(input: ProcessingInput) = {
    operation.detail match {
      case SetDxStatusOperationDetail(partinfo) ⇒
        if (updatecrt)
          SetDxStatusToWork(Identifier(partinfo.mtbPartNumber, partinfo.mtbPartIndex, partinfo.vwPartNumber, partinfo.vwKStand))
        else
          Success(CurrentDxStatus("03"))
    }

  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    Success(input)
  }

}