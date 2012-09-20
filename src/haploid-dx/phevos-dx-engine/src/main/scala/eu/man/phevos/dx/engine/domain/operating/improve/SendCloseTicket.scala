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
import dx.improve.ImproveService.{ TicketData, SendCloseTicket }
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "send-close-ticket-operation-detail")
case class SendCloseTicketOperationDetail(

  @xmlAttribute(required = true) dummy: String) extends OperationDetail("sendcloseticket") {

  def this() = this(null)
}

class SendCloseTicketOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase with Local with FileHandler {

  type PreProcessingInput = TicketData

  type ProcessingInput = TicketData

  type PostProcessingInput = Unit

  type PostProcessingOutput = Unit

  protected[this] def doPreProcessing(input: PreProcessingInput) = Success(input)

  protected[this] def doProcessing(input: ProcessingInput) = {
    operation.detail match {
      case SendCloseTicketOperationDetail(dummy) ⇒
        if (sendticket) {
          SendCloseTicket(input)
        } else {
          info("'send-ticket' is set to 'off'. No closed ticket will be sent to Improve.")
          Success(())
        }
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = Success(())

} 
