package eu.man.phevos

package dx

package engine

package domain

package operating

package improve

import java.nio.file.Path
import javax.xml.bind.annotation.XmlType
import akka.event.LoggingAdapter
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local, FileHandler }
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.event.OperationCreate
import dx.improve.ImproveService.{ TicketData, SendOpenTicket }
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "send-open-ticket-operation-detail")
case class SendOpenTicketOperationDetail(

  @xmlAttribute(required = true) dummy: String)

  extends OperationDetail("sendopenticket") {

  def this() = this(null)
}

class SendOpenTicketOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local {

  type PreProcessingInput = TicketData

  type ProcessingInput = TicketData

  type PostProcessingInput = Unit

  type PostProcessingOutput = Unit

  protected[this] def doPreProcessing(input: PreProcessingInput) = Success(input)

  protected[this] def doProcessing(input: ProcessingInput) = {

    operation.detail match {
      case SendOpenTicketOperationDetail(dummy) ⇒
        if (sendticket) {
          SendOpenTicket(input)
        } else {
          info("'send-ticket' is set to 'off'. No open ticket will be sent to Improve.")
          Success(())
        }
    }

  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = Success(null)

}
