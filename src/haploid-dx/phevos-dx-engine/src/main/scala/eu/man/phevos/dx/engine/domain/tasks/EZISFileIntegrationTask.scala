package eu.man.phevos.dx.engine.domain.tasks
import com.ibm.haploid.dx.engine.domain.TaskFSM
import com.ibm.haploid.dx.engine.domain.TaskDetail
import com.ibm.haploid.dx.engine.domain.DomainObjectState
import com.ibm.haploid.dx.engine.event.TaskCreate
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import eu.man.phevos.dx.util.interfaces.MTBPartFile
import eu.man.phevos.dx.util.interfaces.PartInfo
import com.ibm.haploid.dx.engine.domain.TaskData
import com.ibm.haploid.core.inject.BindingModule
import javax.xml.bind.annotation.XmlType
import eu.man.phevos.dx.engine.domain.operating.kvs.CheckDVForEZISFileExistsOperationDetail
import eu.man.phevos.dx.crt.CrtServices.Sheets
import com.ibm.haploid.dx.engine.domain.TaskResultDetail
import eu.man.phevos.dx.engine.domain.exceptions._
import eu.man.phevos.dx.engine.domain.operating.kvs.UploadKVSOperationDetail
import eu.man.phevos.dx.engine.domain.operating.crt.ValidSheetsOperationDetail
import eu.man.phevos.dx.engine.domain.operating.kvs.CheckDVForEZISFileExistsOutput
import eu.man.phevos.dx.engine.domain.operating.kvs.UpdateKStandOperationDetail
import eu.man.phevos.dx.engine.domain.operating.kvs.CheckDVWithSameOrHigherIndexExistsOperationDetail
import eu.man.phevos.dx.util.interfaces.EZISSheetMetadata
import com.ibm.haploid.core.service.Failure
import com.ibm.haploid.core.service.Success
import eu.man.phevos.dx.util.interfaces.EZISMetadata
import eu.man.phevos.dx.engine.domain.operating.kvs.AttachPageDVToKStandOperationDetail
import eu.man.phevos.dx.engine.domain.operating.kvs.AttachDVOfHighestIndexOfSheetToKStandOperationDetail
import eu.man.phevos.dx.util.interfaces.MTBPartMetadata
import collection.mutable.ListBuffer

/**
 *
 */
@XmlType(name = "ezis-file-integration-task-detail")
case class EZISFileIntegrationTaskDetail(

  @xmlElement(required = true) partInfo: PartInfo)

  extends TaskDetail {

  def this() = this(null)

  val name = "ezis-file-integration"

}

/**
 *
 */
case class EZISFileIntegrationTaskFSM(task: TaskCreate)(implicit bindingmodule: BindingModule)

  extends TaskFSM {

  sealed trait EZISFileIntegrationTaskState extends DomainObjectState
  case object WaitingForCheckTiffDVExists extends EZISFileIntegrationTaskState
  case object WaitingForUploadAndAttaching extends EZISFileIntegrationTaskState
  case object WaitingForUpdateKStand extends EZISFileIntegrationTaskState
  case object WaitingForValidSheets extends EZISFileIntegrationTaskState
  case object WaitingForCheckSheetsExists extends EZISFileIntegrationTaskState

  sealed trait EZISFileIntegrationTaskData extends TaskData
  case class ValidSheetsDetails(partinfo: PartInfo) extends EZISFileIntegrationTaskData
  case class TiffDVExistsDetails(partinfo: PartInfo, files: Map[EZISMetadata, MTBPartFile], results: Map[EZISMetadata, Boolean]) extends EZISFileIntegrationTaskData
  case class UploadAndAttachingDetails(partinfo: PartInfo, executions: List[(Any, Execution)]) extends EZISFileIntegrationTaskData
  case class CheckSheetsExistsDetails(partinfo: PartInfo, sheets: Sheets, count: Int, done: Int, failures: Int, failed: ListBuffer[String]) extends EZISFileIntegrationTaskData

  val (updateKStandOp, getValidSheets) = {
    task.detail match {
      case EZISFileIntegrationTaskDetail(partinfo) ⇒ (
        create(UpdateKStandOperationDetail(partinfo), "update-kstand"),
        create(ValidSheetsOperationDetail(partinfo), "get-valid-sheets"))
    }
  }

  startWithInput {
    case (EZISFileIntegrationTaskDetail(partInfo), _files: List[_]) if (_files.size > 0) =>
      val files = _files.map(_.asInstanceOf[MTBPartFile])
      val opDetail = CheckDVForEZISFileExistsOperationDetail(partInfo)

      files.zipWithIndex.foreach {
        case (file, i) ⇒
          create(opDetail, "check-tiff-dv-exists-" + (i + 1)).execute(file.metadata, WaitingForCheckTiffDVExists)
      }

      val map = files.map(file ⇒ (file.metadata.asInstanceOf[EZISMetadata], file)).toMap

      goto(WaitingForCheckTiffDVExists) using (TiffDVExistsDetails(partInfo, map, Map()))
    case (EZISFileIntegrationTaskDetail(partinfo), _) =>
      if (partinfo.dxStatus.toInt != 4) {
        finalize(TaskResultDetail(Success()))
      } else {
        getValidSheets.execute((), WaitingForValidSheets, ValidSheetsDetails(partinfo))
      }
  }

  succeeded(WaitingForCheckTiffDVExists) {
    case (CheckDVForEZISFileExistsOutput(exists, file), TiffDVExistsDetails(partinfo, files, map)) ⇒

      val details = TiffDVExistsDetails(partinfo, files, map + (file -> exists))

      if (details.results.size < files.size)
        stay using details
      else {
        val uploadDetail = UploadKVSOperationDetail(partinfo)
        val attachDetail = AttachPageDVToKStandOperationDetail(partinfo)

        details.results.zipWithIndex.map {
          case ((file, true), i) ⇒
            (file, create(attachDetail, "attach-page-dv-to-kstand-file-" + (i + 1)))
          case ((file, false), i) ⇒
            (files(file), create(uploadDetail, "upload-" + (i + 1)))
        } toList match {
          case ((file, exe) :: executions) ⇒
            exe.execute(file, WaitingForUploadAndAttaching, UploadAndAttachingDetails(partinfo, executions))
          case _ ⇒
            finalize(TaskResultDetail(Failure(new Exception("Engine Exception."))))
        }
      }
  }

  succeeded(WaitingForUploadAndAttaching) {
    case (true, UploadAndAttachingDetails(partinfo, List())) ⇒
      updateKStandOp.execute(WaitingForUpdateKStand)

    case (true, UploadAndAttachingDetails(partinfo, (file, exe) :: executions)) ⇒
      exe.execute(file, WaitingForUploadAndAttaching, UploadAndAttachingDetails(partinfo, executions))

    case (false, UploadAndAttachingDetails(partinfo, _)) ⇒
      finalize(TaskResultDetail(P3B3_KStandUpdateFailed))
  }

  succeeded(WaitingForValidSheets) {
    case (Sheets(sheets), ValidSheetsDetails(partinfo)) ⇒
      if (sheets.size > 0) {
        val opDetail =
          if (partinfo.isCOP)
            CheckDVWithSameOrHigherIndexExistsOperationDetail(partinfo)
          else
            CheckDVForEZISFileExistsOperationDetail(partinfo)

        sheets.zipWithIndex.foreach {
          case (sheet, i) ⇒
            create(opDetail, "check-sheets-exists-" + (i + 1)).execute(sheet, WaitingForCheckSheetsExists)
        }

        goto(WaitingForCheckSheetsExists) using CheckSheetsExistsDetails(partinfo, Sheets(sheets), sheets.size, 0, 0, ListBuffer())
      } else {
        updateKStandOp.execute(WaitingForUpdateKStand)
      }
  }

  succeeded(WaitingForCheckSheetsExists) {
    case (result @ (_: Boolean, _), CheckSheetsExistsDetails(partinfo, Sheets(sheets), count, done, fails, failedSheets)) ⇒
      val details = {
        result match {
          case (true, _) ⇒
            CheckSheetsExistsDetails(partinfo, Sheets(sheets), count, done + 1, fails, failedSheets )
          case (false, _) ⇒
            CheckSheetsExistsDetails(partinfo, Sheets(sheets), count, done, fails + 1, failedSheets += result._2.toString)
        }
      }

      if (details.count == details.done) {
        val opDetail =
          if (partinfo.isCOP)
            AttachDVOfHighestIndexOfSheetToKStandOperationDetail(partinfo)
          else
            AttachPageDVToKStandOperationDetail(partinfo)

        sheets.zipWithIndex.map {
          case (sheet, i) ⇒
            (sheet, create(opDetail, "attach-sheets-in-kvs-" + (i + 1)))
        } match {
          case ((sheet, exe) :: executions) ⇒
            exe.execute(sheet, WaitingForUploadAndAttaching, UploadAndAttachingDetails(partinfo, executions))
          case _ ⇒
            finalize(TaskResultDetail(Failure(new Exception("Engine Exception."))))
        }
      } else if (details.count == (details.done + details.failures)) {
        if (partinfo.isCOP) {
          finalize(TaskResultDetail(Failure(new Exception(details.failed.toList.mkString("Following sheets exist only with a lower revision in KVS : ", " ; ", "")))))
        } else {
          finalize(TaskResultDetail(Failure(new Exception("Sheet doesn't exist in KVS (" + details + ")"))))
        }

      } else {
        stay using details
      }
  }

  succeeded(WaitingForUpdateKStand) {
    case (true, _) ⇒
      finalize(TaskResultDetail(Success(())))
  }

}