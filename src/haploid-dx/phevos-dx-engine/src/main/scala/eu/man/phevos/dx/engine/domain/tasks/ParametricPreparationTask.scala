package eu.man

package phevos

package dx

package engine

package domain

package tasks

import javax.xml.bind.annotation.XmlType

import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.{TaskResultDetail, TaskFSM, TaskDetail, TaskData, DomainObjectState}
import com.ibm.haploid.dx.engine.event.TaskCreate

import dx.engine.domain.operating.catia5.ParametricData
import dx.engine.domain.operating.kvs.GetKVSFilenameForParametricOperationDetail
import dx.engine.domain.operating.util.{ZipInput, ZipFile, CreateZipOperationDetail}
import dx.util.interfaces.PartInfo

/**
 * Details Phevos DX Parametric Extraction Task.
 */
@XmlType(name = "parametric-preparation-task-detail")
case class ParametricPreparationTaskDetail(
  @xmlElement(required = true) partinput: PartInfo)

  extends TaskDetail {

  private def this() = this(null)

  val name = "parametric-preparation-task"

}

/**
 *
 */
case class ParametricPreparationTaskFSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskFSM {

  /**
   * Define task states
   */
  sealed trait ParametricPreparationTaskState extends DomainObjectState
  case object WaitingFor_GetKVSFileName extends ParametricPreparationTaskState
  case object WaitingFor_CreateKVSZip extends ParametricPreparationTaskState

  /**
   * Define task data classes
   */
  sealed abstract class ParametricPreparationTaskData extends TaskData
  case class ParametricPreparationTaskDetails(partinfo: PartInfo, parametricdata: ParametricData) extends ParametricPreparationTaskData

  /**
   * Create operations
   */
  val (getKVSFileName_Operation, createKVSZip_Operation) = {
    task.detail match {
      case ParametricPreparationTaskDetail(partinput) => (
        create(new GetKVSFilenameForParametricOperationDetail(partinput), "get-kvs-filename-for-parametric"),
        create(new CreateZipOperationDetail(true), "create-kvs-zip"))
    }
  }

  /**
   * Define entry point
   */
  startWithInput {
    case (ParametricPreparationTaskDetail(partinput), parametricdata: ParametricData) =>
      getKVSFileName_Operation.execute(partinput, WaitingFor_GetKVSFileName, ParametricPreparationTaskDetails(partinput, parametricdata))
  }

  /**
   * Define success cases if operations
   */
  succeeded(WaitingFor_GetKVSFileName) {
    case (kvsfilename: String, ParametricPreparationTaskDetails(partinput, parametricdata)) =>
      createKVSZip_Operation.execute(ZipInput(parametricdata.path.toAbsolutePath, kvsfilename), WaitingFor_CreateKVSZip)
  }

  /**
   * Finalize Task
   */
  finalize(WaitingFor_CreateKVSZip) {
    case (result: ZipFile, _) => {
      TaskResultDetail(Success(result))
    }
  }
}

