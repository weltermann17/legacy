package eu.man

package phevos

package dx

package engine

package domain

package jobs

import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.core.service.{ Success, Failure }
import com.ibm.haploid.dx.engine.domain.flow.ExecutionData
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.{ JobResultDetail, JobFSM ⇒ BaseJobFSM, JobDetail ⇒ BaseJobDetail, DomainObjectState }
import com.ibm.haploid.dx.engine.event.JobCreate

import dx.engine.domain.exceptions.{ PhevosException, P0A2_UnexpectedResult }
import dx.engine.domain.tasks.{ ReturnSuccess, ReturnFailure, ParametricFileTaskDetail, InWork, EZISFileTaskDetail, DMUFileTaskDetail }
import dx.util.interfaces.{ PartInfo, MTBPartFile }
import javax.xml.bind.annotation.XmlType
import tasks.{ StatusReportTaskDetail, CheckTaskDetail }

/**
 * Details of a DX job.
 */
@XmlType(name = "phevos-dx-job-detail")
case class JobDetail(

  @xmlElement partinfo: PartInfo)

  extends BaseJobDetail {

  val name = "phevos-job"

  def this() = this(null)

}

/**
 *
 */
case class JobFSM(

  job: JobCreate)(

    implicit bindingmodule: BindingModule)

  extends BaseJobFSM {

  sealed trait JobState extends DomainObjectState
  case object WaitingForDXStatusInWork extends JobState
  case object WaitingForCheckTask extends JobState
  case object WaitingForEZISFileProcessing extends JobState
  case object WaitingForParametricFileProcessing extends JobState
  case object WaitingForDMUFileProcessing extends JobState
  case object WaitingForReportResult extends JobState

  sealed trait JobData extends ExecutionData
  case class JobDetails(parInfo: PartInfo, eePart: Boolean) extends JobData
  case class WaitingForTiffPreperationData(partInfo: PartInfo, eePart: Boolean, downloadedTiffs: List[MTBPartFile], preparedTiffs: List[MTBPartFile]) extends JobData // TODO: Add Failure counts?

  val (setDXStatus, checkTask, reportResult, ezisFileProcessing, parametricFileProcessing, dmuFileProcessing) = {
    job.detail match {
      case detail @ JobDetail(_) ⇒
        (
          create(StatusReportTaskDetail(detail.partinfo), "set-dx-status"),
          create(CheckTaskDetail(detail.partinfo), "check-part"),
          create(StatusReportTaskDetail(detail.partinfo), "report-result"),
          create(EZISFileTaskDetail(detail.partinfo), "pxb-ezis-file-process"),
          create(ParametricFileTaskDetail(detail.partinfo), "pxa-parametric-file-process"),
          create(DMUFileTaskDetail(detail.partinfo), "pxc-dmu-file-process"))
    }
  }

  start {
    case detail @ JobDetail(_) ⇒
      setDXStatus.execute(InWork, WaitingForDXStatusInWork, JobDetails(detail.partinfo, detail.partinfo.isEEPartnumber))
  }

  succeeded(WaitingForDXStatusInWork) {
    case ((), _) ⇒
      checkTask.execute((), WaitingForCheckTask)
  }

  succeeded(WaitingForCheckTask) {
    case (Success(_), _) ⇒
      ezisFileProcessing.execute((), WaitingForEZISFileProcessing)
    case (Failure(e @ PhevosException(_, _)), _) ⇒
      reportResult.execute(ReturnFailure(e), WaitingForReportResult)
  }

  succeeded(WaitingForEZISFileProcessing) {
    case ((), JobDetails(_, false)) ⇒
      dmuFileProcessing.execute((), WaitingForDMUFileProcessing)
    case ((), JobDetails(_, true)) ⇒
      reportResult.execute(ReturnSuccess, WaitingForReportResult)
  }

  succeeded(WaitingForDMUFileProcessing) {
    case ((), _) ⇒
      parametricFileProcessing.execute((), WaitingForParametricFileProcessing)
  }

  succeeded(WaitingForParametricFileProcessing) {
    case ((), _) ⇒
      reportResult.execute(ReturnSuccess, WaitingForReportResult)
  }

  finalize(WaitingForReportResult) {
    case (Success(_), _) ⇒
      println("Job finished successfully : " + job)
      log.info("Job finished successfully")
      new JobResultDetail(Success())
    case (Failure(e), _) ⇒
      println("Job finished with Failure: " + e.getMessage + " : " + job)
      log.error("Job finished with Failure: " + e.getMessage)
      new JobResultDetail(Failure(e))
  } failed {
    case (e, _) ⇒
      println("Job finished with Exception: " + e.getMessage + " : " + job)
      log.error("Job finished with Exception: " + e.getMessage)
      new JobResultDetail(Failure(e))
  }

  failure {
    case (pe @ PhevosException(_, _), _) ⇒
      reportResult.execute(ReturnFailure(pe), WaitingForReportResult)
    case (e, _) ⇒
      reportResult.execute(ReturnFailure(P0A2_UnexpectedResult(e)), WaitingForReportResult)
  }

}
