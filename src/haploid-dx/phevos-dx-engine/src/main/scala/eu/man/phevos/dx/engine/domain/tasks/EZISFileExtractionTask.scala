package eu.man

package phevos

package dx

package engine

package domain

package tasks

import scala.util.matching.Regex
import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.core.service.{ Success }
import com.ibm.haploid.dx.engine.domain.binding.{ xmlElement, xmlAttribute }
import com.ibm.haploid.dx.engine.domain.{ TaskResultDetail, TaskFSM, TaskDetail, TaskData, DomainObjectState }
import com.ibm.haploid.dx.engine.event.TaskCreate
import dx.crt.CrtServices.{ Sheets, Identifier }
import dx.ezis.EzisServices._
import dx.gep.TiffUrlResult
import dx.gep.TiffUrlResult
import eu.man.phevos.dx.util.interfaces.{ PartInfo, MTBPartFile }
import javax.xml.bind.annotation.XmlType
import operating.crt.ValidSheetsOperationDetail
import operating.ezis.UnstampedTiffsOperationDetail
import operating.gep.GetTiffUrlsOperationDetail
import eu.man.phevos.dx.engine.domain.exceptions.P1B1_NoEZISFiles

/**
 * Details Phevos DX Tiff Extraction Task.
 */
@XmlType(name = "ezis-file-extraction-task-detail")
case class EZISFileExtractionTaskDetail(
  @xmlElement(required = true) partInfo: PartInfo)

  extends TaskDetail {

  private def this() = this(null)

  val name = "ezis-file-extraction-task"

}

/**
 *
 */
case class EZISFileExtractionTaskFSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskFSM {

  /**
   * Define task states
   */
  sealed trait EZISFileExtractionTaskState extends DomainObjectState
  case object WaitingFor_ValidSheets extends EZISFileExtractionTaskState
  case object WaitingFor_GetDXStatus extends EZISFileExtractionTaskState
  case object WaitingFor_DownloadUnstampedTiff extends EZISFileExtractionTaskState

  /**
   * Define task data classes
   */
  sealed abstract class EZISFileExtractionTaskData extends TaskData
  case class TiffTaskDetails(partinfo: PartInfo) extends EZISFileExtractionTaskData

  /**
   * Create operations
   */

  val (getValidSadisSheets_Operation, getTiffUrls_Operation, downloadUnstampedTiffs_Operation) = {
    task.detail match {
      case EZISFileExtractionTaskDetail(partinfo) ⇒ (
        create(new ValidSheetsOperationDetail(partinfo), "valid-sadis-sheets"),
        create(new GetTiffUrlsOperationDetail(partinfo), "tiff-urls"),
        create(new UnstampedTiffsOperationDetail("Get tiff urls for part"), "download"))
    }
  }

  /**
   * Define entry point
   */
  start {
    case detail @ EZISFileExtractionTaskDetail(partinfo) if (partinfo.dxStatus.toInt == 4) ⇒
    	finalize(TaskResultDetail(Success(List[MTBPartFile]())))
    	
    case detail @ EZISFileExtractionTaskDetail(partinfo) ⇒
      getValidSadisSheets_Operation.execute(WaitingFor_ValidSheets, TiffTaskDetails(partinfo))
  }

  /**
   * Define success cases if operations
   */
  succeeded(WaitingFor_ValidSheets) {
    case (sheets: Sheets, TiffTaskDetails(partinfo)) ⇒
      partinfo.dxStatus match {
        case "03" ⇒
          if (sheets.sheets.size > 0) {
            val ezisinput = MultipleTiffRequests(
              sheets.sheets.map(x ⇒ TiffRequest(partinfo.mtbPartNumber, x.sheetIndex, x.sheetNumber))
                .toList)

            downloadUnstampedTiffs_Operation.execute(ezisinput, WaitingFor_DownloadUnstampedTiff)
          } else {
            finalize(TaskResultDetail(P1B1_NoEZISFiles))
          }
      }
  }

  /**
   * Finalize Task
   */
  finalize(WaitingFor_DownloadUnstampedTiff) {
    case (result: MultipleTiffFiles, _) ⇒ {
      TaskResultDetail(Success(result.files))
    }
  }

}

