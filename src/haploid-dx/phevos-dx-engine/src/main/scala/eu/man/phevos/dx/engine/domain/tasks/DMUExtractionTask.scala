package eu.man

package phevos

package dx

package engine

package domain

package tasks

import scala.util.matching.Regex
import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.core.service.{ Success, Failure }
import com.ibm.haploid.dx.engine.domain.binding.{ xmlElement, xmlAttribute }
import com.ibm.haploid.dx.engine.domain.{ TaskResultDetail, TaskFSM, TaskDetail, TaskData, DomainObjectState }
import com.ibm.haploid.dx.engine.event.TaskCreate
import com.ibm.haploid.core.service.SimpleServiceException
import javax.xml.bind.annotation._
import java.nio.file.Path
import dx.engine.domain.operating.gep.{ GetPartMetadataOperationDetail, GetCATPartOperationDetail }
import dx.ezis.EzisServices._
import dx.util.interfaces.{ PartInfo }
import dx.gep.{ PartMetadataInput, PartMetadata, CATPartFileResult }

/**
 * Details Phevos DX DMU Extraction Task.
 */
@XmlType(name = "dmu-extraction-task-detail")
case class DMUExtractionTaskDetail(
  @xmlElement(required = true) partinput: PartMetadataInput)

  extends TaskDetail {

  private def this() = this(null)

  val name = "dmu-extraction-task"

}

/**
 *
 */
case class DMUExtractionTaskFSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskFSM {

  /**
   * Define task states
   */
  sealed trait DMUExtractionTaskState extends DomainObjectState
  case object WaitingFor_GetPartMetadata extends DMUExtractionTaskState
  case object WaitingFor_DownloadCATPart extends DMUExtractionTaskState

  /**
   * Define task data classes
   */

  /**
   * Create operations
   */
  val (getPartMetadata_Operation, downloadCATPart_Operation) = {
    task.detail match {
      case DMUExtractionTaskDetail(partinput) ⇒ (
        create(new GetPartMetadataOperationDetail(partinput), "getpartmetadata"),
        create(new GetCATPartOperationDetail(partinput.partnumber, partinput.partindex), "get-catpart"))
    }
  }

  /**
   * Define entry point
   */
  start {
    case DMUExtractionTaskDetail(partinput) ⇒
      getPartMetadata_Operation.execute((), WaitingFor_GetPartMetadata)
  }

  /**
   * Define success cases if operations
   */
  succeeded(WaitingFor_GetPartMetadata) {
    case (partMetadata: PartMetadata, _) ⇒
      if (partMetadata.isAssembly) {
        finalize(TaskResultDetail(Success(true)))
      } else if (!partMetadata.hasCATParts) {
        finalize(TaskResultDetail(Failure(SimpleServiceException("No CATPart found."))))
      } else if (!partMetadata.isReleased) {
        finalize(TaskResultDetail(Failure(SimpleServiceException("Not released."))))
      } else {
        downloadCATPart_Operation.execute(partMetadata.partfilename, WaitingFor_DownloadCATPart)
      }
  }

  /**
   * Finalize Task
   */
  finalize(WaitingFor_DownloadCATPart) {
    case (result: CATPartFileResult, _) ⇒ {
      TaskResultDetail(Success(false, result))
    }
  }

}

