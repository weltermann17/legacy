package eu.man

package phevos

package dx

package engine

package domain

package tasks

import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.core.service.{Success, Failure}
import com.ibm.haploid.core.service.SimpleServiceException
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.{TaskResultDetail, TaskFSM, TaskDetail, TaskData, DomainObjectState}
import com.ibm.haploid.dx.engine.event.TaskCreate

import dx.engine.domain.operating.kvs.{ValidateKStandOperationDetail, UploadKVSOperationDetail, CheckPartNumberExistsOperationDetail, CheckParametricDocumentVersionExistsOperationDetail}
import dx.engine.domain.operating.util.ZipFile
import dx.util.interfaces.PartInfo
import eu.man.phevos.dx.util.interfaces.MTBPartFile
import javax.xml.bind.annotation.{XmlType, XmlRootElement}

/**
 * Details Phevos DX Parametric Extraction Task.
 */
@XmlType(name = "parametric-integration-task-detail")
case class ParametricIntegrationTaskDetail(
  @xmlElement(required = true) partinput: PartInfo)

  extends TaskDetail {

  private def this() = this(null)

  val name = "parametric-integration-task"

}

/**
 *
 */
case class ParametricIntegrationTaskFSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskFSM {

  /**
   * Define task states
   */
  sealed trait ParametricIntegrationTaskState extends DomainObjectState
  case object WaitingFor_CheckPartNumberExistsInKVS extends ParametricIntegrationTaskState
  case object WaitingFor_ValidateKStand extends ParametricIntegrationTaskState
  case object WaitingFor_CheckParametricVersionDoesNotExist extends ParametricIntegrationTaskState
  case object WaitingFor_UploadToKVS extends ParametricIntegrationTaskState

  /**
   * Define task data classes
   */
  sealed abstract class ParametricIntegrationTaskDataMain extends TaskData
  case class ParametricIntegrationTaskData(zipfile: ZipFile, partinput: PartInfo) extends ParametricIntegrationTaskDataMain

  /**
   * Create operations
   */
  val (checkPartNumber_Operation, validateKStand_Operation, checkParametricVersion_Operation, uploadToKVS_Operation) = {
    task.detail match {
      case ParametricIntegrationTaskDetail(partinput) ⇒ (
        create(new CheckPartNumberExistsOperationDetail(partinput), "check-part-number-exists"),
        create(new ValidateKStandOperationDetail(partinput), "validate-kstand"),
        create(new CheckParametricDocumentVersionExistsOperationDetail(partinput), "check-parametric-document-exists"),
        create(new UploadKVSOperationDetail(partinput), "upload-parametric-zip-to-kvs"))
    }
  }

  /**
   * Define entry point
   */
  startWithInput {
    case (ParametricIntegrationTaskDetail(partinput), zipfile: ZipFile) ⇒
      checkPartNumber_Operation.execute(WaitingFor_CheckPartNumberExistsInKVS, ParametricIntegrationTaskData(zipfile, partinput))
  }

  /**
   * Define success cases if operations
   */
  succeeded(WaitingFor_CheckPartNumberExistsInKVS) {
    case (partnumberexists: Boolean, ParametricIntegrationTaskData(zipfile, partinput)) ⇒
      if (partnumberexists) {
        validateKStand_Operation.execute(WaitingFor_ValidateKStand, ParametricIntegrationTaskData(zipfile, partinput))
      } else {
        finalize(TaskResultDetail(Failure(SimpleServiceException("Part number does not exist in KVS."))))
      }
  }

  succeeded(WaitingFor_ValidateKStand) {
    case (kstandok: Boolean, ParametricIntegrationTaskData(zipfile, partinput)) ⇒
      if (kstandok) {
        checkParametricVersion_Operation.execute(WaitingFor_CheckParametricVersionDoesNotExist, ParametricIntegrationTaskData(zipfile, partinput))
      } else {
        finalize(TaskResultDetail(Failure(SimpleServiceException("KStand is not ok."))))
      }
  }

  succeeded(WaitingFor_CheckParametricVersionDoesNotExist) {
    case (parametricversionexists: Boolean, ParametricIntegrationTaskData(zipfile, partinput)) ⇒
      if (parametricversionexists) {
        finalize(TaskResultDetail(Success()))
      } else {
        uploadToKVS_Operation.execute(MTBPartFile(zipfile.zipfile), WaitingFor_UploadToKVS)
      }
  }

  /**
   * Finalize Task
   */
  finalize(WaitingFor_UploadToKVS) {
    case (result: Boolean, _) ⇒ {
      TaskResultDetail(Success(()))
    }
  }

}

