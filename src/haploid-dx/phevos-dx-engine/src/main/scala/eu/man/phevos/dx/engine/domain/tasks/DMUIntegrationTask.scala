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
import dx.ezis.EzisServices._
import dx.util.interfaces.{ PartInfo }
import dx.engine.domain.operating.kvs.{ CheckPartNumberExistsOperationDetail, ValidateKStandOperationDetail, CheckDMUDocumentVersionExistsOperationDetail, UploadKVSOperationDetail }
import dx.engine.domain.operating.util.{ TarFile }
import eu.man.phevos.dx.util.interfaces.MTBPartFile

/**
 * Details Phevos DX DMU Extraction Task.
 */
@XmlType(name = "dmu-integration-task-detail")
case class DMUIntegrationTaskDetail(
  @xmlElement(required = true) partinput: PartInfo)

  extends TaskDetail {

  private def this() = this(null)

  val name = "dmu-integration-task"

}

/**
 *
 */
case class DMUIntegrationTaskFSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskFSM {

  /**
   * Define task states
   */
  sealed trait DMUIntegrationTaskState extends DomainObjectState
  case object WaitingFor_CheckPartNumberExistsInKVS extends DMUIntegrationTaskState
  case object WaitingFor_ValidateKStand extends DMUIntegrationTaskState
  case object WaitingFor_CheckDMUVersionDoesNotExist extends DMUIntegrationTaskState
  case object WaitingFor_UploadToKVS extends DMUIntegrationTaskState

  /**
   * Define task data classes
   */
  sealed abstract class DMUIntegrationTaskDataMain extends TaskData
  case class DMUIntegrationTaskData(tarfile: TarFile, partinput: PartInfo) extends DMUIntegrationTaskDataMain

  /**
   * Create operations
   */
  val (checkPartNumber_Operation, validateKStand_Operation, checkDMUVersion_Operation, uploadToKVS_Operation) = {
    task.detail match {
      case DMUIntegrationTaskDetail(partinput) ⇒ (
        create(new CheckPartNumberExistsOperationDetail(partinput), "check-part-number-exists"),
        create(new ValidateKStandOperationDetail(partinput), "validate-kstand"),
        create(new CheckDMUDocumentVersionExistsOperationDetail(partinput), "check-dmu-document-exists"),
        create(new UploadKVSOperationDetail(partinput), "upload-dmu-tar-to-kvs"))
    }
  }

  /**
   * Define entry point
   */
  startWithInput {
    case (DMUIntegrationTaskDetail(partinput), tarfile: TarFile) ⇒
      checkPartNumber_Operation.execute(WaitingFor_CheckPartNumberExistsInKVS, DMUIntegrationTaskData(tarfile, partinput))
  }

  /**
   * Define success cases if operations
   */
  succeeded(WaitingFor_CheckPartNumberExistsInKVS) {
    case (partnumberexists: Boolean, DMUIntegrationTaskData(tarfile, partinput)) ⇒
      if (partnumberexists) {
        validateKStand_Operation.execute(WaitingFor_ValidateKStand, DMUIntegrationTaskData(tarfile, partinput))
      } else {
        finalize(TaskResultDetail(Failure(SimpleServiceException("Part number does not exist in KVS."))))
      }
  }

  succeeded(WaitingFor_ValidateKStand) {
    case (kstandok: Boolean, DMUIntegrationTaskData(tarfile, partinput)) ⇒
      if (kstandok) {
        checkDMUVersion_Operation.execute(WaitingFor_CheckDMUVersionDoesNotExist, DMUIntegrationTaskData(tarfile, partinput))
      } else {
        finalize(TaskResultDetail(Failure(SimpleServiceException("KStand is not ok."))))
      }
  }

  succeeded(WaitingFor_CheckDMUVersionDoesNotExist) {
    case (dmuversionexists: Boolean, DMUIntegrationTaskData(tarfile, partinput)) ⇒
      if (dmuversionexists) {
        finalize(TaskResultDetail(Success()))
      } else {
        uploadToKVS_Operation.execute(MTBPartFile(tarfile.tarfile), WaitingFor_UploadToKVS, DMUIntegrationTaskData(tarfile, partinput))
      }
  }

  /**
   * Finalize Task
   */
  finalize(WaitingFor_UploadToKVS) {
    case (result: Boolean, _) ⇒ {
      TaskResultDetail(Success())
    }
  }

}

