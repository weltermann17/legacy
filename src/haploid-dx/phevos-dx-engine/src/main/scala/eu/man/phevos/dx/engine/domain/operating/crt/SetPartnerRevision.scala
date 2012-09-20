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
import dx.crt.CrtServices.{ SetKstandDrawingDateDxStatus, Service2Identifier }
import eu.man.phevos.dx.util.interfaces.PartInfo
import java.text.SimpleDateFormat
import java.sql.Timestamp
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "setpartnerrevision-operation-detail")
case class SetPartnerPartRevisionOperationDetail(

  @xmlElement(required = true) partinfo: PartInfo)

  extends OperationDetail("setpartnerrevision") {

  def this() = this(null)
}

case class SetPartnerPartRevisionInput(

  dxStatus: Option[String],

  kStand: String,

  drawingdate: Long)

class SetPartnerPartRevisionOperator(
  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase 
  
  with Local {

  type PreProcessingInput = SetPartnerPartRevisionInput

  type ProcessingInput = SetPartnerPartRevisionInput

  type PostProcessingInput = Unit

  type PostProcessingOutput = Unit

  protected[this] def doPreProcessing(input: PreProcessingInput) = {
    Success(input)
  }

  protected[this] def doProcessing(input: ProcessingInput) = {
    operation.detail match {

      case SetPartnerPartRevisionOperationDetail(partinfo) ⇒
        val dxStatus = input.dxStatus match {
          case None ⇒
            false
          case Some(s) ⇒
            true
        }

        val df = new SimpleDateFormat("dd.MM.YYYY")
        val drawingDate = df.format(new Timestamp(input.drawingdate))

        log.debug("DRAWING DATE " + drawingDate)

        if (updatecrt)
          SetKstandDrawingDateDxStatus(Service2Identifier(partinfo.mtbPartNumber, partinfo.mtbPartIndex, partinfo.vwPartNumber, dxStatus, input.kStand, drawingDate))
        else
          Success(())

    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    Success(input)
  }

}