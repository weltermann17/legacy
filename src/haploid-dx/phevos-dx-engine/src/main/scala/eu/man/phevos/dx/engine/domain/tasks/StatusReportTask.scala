package eu.man

package phevos

package dx

package engine

package domain

package tasks

import java.sql.Timestamp
import java.text.SimpleDateFormat
import javax.xml.bind.annotation.XmlType
import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.core.service.{ Success, Failure }
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.{ TaskResultDetail, TaskFSM, TaskDetail, TaskData, DomainObjectState }
import com.ibm.haploid.dx.engine.event.TaskCreate
import eu.man.phevos.dx.engine.domain.exceptions.{ PhevosException, P0C2_DrawingDate, P0C1_KStand }
import eu.man.phevos.dx.engine.domain.operating.kvs.ResponsibleUserOperationDetail
import eu.man.phevos.dx.util.interfaces.PartInfo
import improve.ImproveService.TicketData
import operating.crt.{ SetPartnerPartRevisionOperationDetail, SetPartnerPartRevisionInput, SetDxStatusOperationDetail }
import operating.improve.{ SendOpenTicketOperationDetail, SendCloseTicketOperationDetail }
import operating.kvs.KStandDrawingDateGetOperationDetail
import eu.man.phevos.dx.kvs.KStandNotFoundException
import eu.man.phevos.dx.engine.domain.exceptions.P0C3_CRTUpdateAfterFailure
import eu.man.phevos.dx.engine.domain.exceptions.P0C4_CRTUpdateAfterSuccess

sealed trait ReportStatus
case object InWork extends ReportStatus
case object ReturnSuccess extends ReportStatus
case class ReturnFailure(ex: PhevosException, processingStarted: Boolean = true) extends ReportStatus

/**
 * Details of a CrtTestTask.
 */
@XmlType(name = "status-report-task-detail")
case class StatusReportTaskDetail(

  @xmlElement(required = true) partInfo: PartInfo)

  extends TaskDetail {

  private def this() = this(null)

  val name = "status-report-task"

}

/**
 *
 */
case class StatusReportTaskFSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskFSM {

  /**
   * Define task states
   */
  sealed trait StatusReportTaskState extends DomainObjectState
  case object WaitingForCheckKStandDrawingDateGet extends StatusReportTaskState
  case object WaitingForUpdateCRT extends StatusReportTaskState
  case object WaitingForSendingTicket extends StatusReportTaskState
  case object WaitingForSetInWork extends StatusReportTaskState
  case object WaitingForSendCloseTicket extends StatusReportTaskState
  case object WaitingForSendOpenTicket extends StatusReportTaskState
  case object WaitingForResponsibleUser extends StatusReportTaskState

  /**
   * Define task data classes
   */
  sealed abstract class StatusReportTaskData extends TaskData
  case class StatusReportTaskDetails(partinfo: PartInfo, status: ReportStatus) extends StatusReportTaskData
  case class WaitingForSendOpenTicketDetails(ex: PhevosException) extends StatusReportTaskData
  case class WaitingForResponsibleUserDetails(partinfo: PartInfo, ex: Option[PhevosException]) extends StatusReportTaskData

  val sendCloseTicketOperator = create(SendCloseTicketOperationDetail(null), "sendcloseticket")
  val sendOpenTicketOperator = create(SendOpenTicketOperationDetail(null), "sendopenticket")

  val (setDxStatusOperator, checkKStndDrwgDtSetOp, updateCRTOp, respUserOp) = task.detail match {
    case StatusReportTaskDetail(partinfo) ⇒ (
      create(SetDxStatusOperationDetail(partinfo), "setdxstatus"),
      create(KStandDrawingDateGetOperationDetail(partinfo), "kstand-drawing-date-get"),
      create(SetPartnerPartRevisionOperationDetail(partinfo), "update-crt"),
      create(ResponsibleUserOperationDetail(partinfo), "get-responsible-user"))
  }

  startWithInput {
    case (StatusReportTaskDetail(partInfo), InWork) ⇒
      setDxStatusOperator.execute(WaitingForSetInWork)

    case (StatusReportTaskDetail(partinfo), status @ (ReturnSuccess | ReturnFailure(_, true))) ⇒
      checkKStndDrwgDtSetOp.execute(WaitingForCheckKStandDrawingDateGet, StatusReportTaskDetails(partinfo, status.asInstanceOf[ReportStatus]))

    case (StatusReportTaskDetail(partinfo), ReturnFailure(ex, false)) ⇒
      sendImproveTicket(partinfo, Some(ex))
  }

  succeeded(WaitingForCheckKStandDrawingDateGet) {
    case ((date: Long, kstand: String), StatusReportTaskDetails(partinfo, ReturnSuccess)) ⇒
      updateCRTOp.execute(SetPartnerPartRevisionInput(Some("06"), kstand, date), WaitingForUpdateCRT)
    case ((date: Long, kstand: String), StatusReportTaskDetails(partinfo, ReturnFailure(_, _))) ⇒
      updateCRTOp.execute(SetPartnerPartRevisionInput(None, kstand, date), WaitingForUpdateCRT)
  } failed {
    case (_: KStandNotFoundException, StatusReportTaskDetails(partinfo, ReturnFailure(ex, _))) =>
      sendImproveTicket(partinfo, Some(ex))
    case (_: Throwable, StatusReportTaskDetails(partinfo, ReturnSuccess)) ⇒
      sendImproveTicket(partinfo, Some(P0C1_KStand))
    case (_: Throwable, StatusReportTaskDetails(partinfo, ReturnFailure(ex, _))) ⇒
      sendImproveTicket(partinfo, Some(P0C2_DrawingDate(ex)))
  }

  succeeded(WaitingForUpdateCRT) {
    case (_, StatusReportTaskDetails(partinfo, ReturnSuccess)) ⇒
      sendImproveTicket(partinfo)
    case (_, StatusReportTaskDetails(partinfo, ReturnFailure(ex, _))) ⇒
      sendImproveTicket(partinfo, Some(ex))
  } failed {
    case (_, StatusReportTaskDetails(partinfo, ReturnFailure(ex, _))) ⇒
      sendImproveTicket(partinfo, Some(P0C3_CRTUpdateAfterFailure(ex)))
    case (_, StatusReportTaskDetails(partinfo, ReturnSuccess)) ⇒
      sendImproveTicket(partinfo, Some(P0C4_CRTUpdateAfterSuccess))
  }

  succeeded(WaitingForResponsibleUser) {
    case (user: String, WaitingForResponsibleUserDetails(partinfo, ex)) ⇒
      sendImproveTicket(partinfo, ex, user)
  } failed {
    case (_, WaitingForResponsibleUserDetails(partinfo, ex)) ⇒
      sendImproveTicket(partinfo, ex, "n/a")
  }

  finalize(WaitingForSendCloseTicket) {
    case ((), _) ⇒
      TaskResultDetail(Success(Success()))
  }

  finalize(WaitingForSendOpenTicket) {
    case (_, WaitingForSendOpenTicketDetails(ex)) ⇒
      TaskResultDetail(Success(Failure(ex)))
  }

  finalize(WaitingForSetInWork) {
    case (result: Any, taskData) ⇒
      log.debug(result.toString)
      TaskResultDetail(Success(()))
  }

  def sendImproveTicket(partinfo: PartInfo, ex: Option[PhevosException] = None) = {
    respUserOp.execute(WaitingForResponsibleUser, WaitingForResponsibleUserDetails(partinfo, ex))
  }

  def sendImproveTicket(partinfo: PartInfo, ex: Option[PhevosException], user: String) = {
    val shortDesc = (partinfo.dxStatus.toInt match {
      case 3 ⇒ "S"
      case _ ⇒ "L"
    }) + " " + partinfo.mtbPartNumber + " " + partinfo.mtbPartIndex

    val desc = "Result of data exchange to KVS\n" +
      "KVS partnumber: " + partinfo.vwPartNumber + "\n" +
      "Result: " + (if (ex.isDefined) "NOK" else "OK") + "\n" +
      "Error text: " + (if (ex.isDefined) ex.get.message else "-") + "\n" +
      "Data owner: " + user + "\n" +
      "Job-Id: " + task.job.id

    val df = new SimpleDateFormat("dd.MM.YYYY")

    val dxProperties = partinfo.unloadFile

    ex match {
      case None ⇒
        sendCloseTicketOperator.execute(TicketData(shortDesc, desc, dxProperties), WaitingForSendCloseTicket)
      case Some(ex) ⇒
        sendOpenTicketOperator.execute(TicketData(shortDesc, desc, dxProperties), WaitingForSendOpenTicket, WaitingForSendOpenTicketDetails(ex))
    }
  }

}

