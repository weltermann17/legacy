package eu.man.phevos.dx.improve

import com.ibm.haploid.core.service._
import java.nio.file.Path

object ImproveService {

  case class TicketData(title: String, description: String, dxProperties: String)

  /**
   * sends an Improve ticket of type open
   */
  object SendOpenTicket extends Service[TicketData, Unit] {
    def doService(info: TicketData): Result[Unit] = Success(OpenTicket(info))
  }

  /**
   * sends an Improve ticket of type close
   */
  object SendCloseTicket extends Service[TicketData, Unit] {
    def doService(info: TicketData): Result[Unit] = Success(CloseTicket(info))
  }
}