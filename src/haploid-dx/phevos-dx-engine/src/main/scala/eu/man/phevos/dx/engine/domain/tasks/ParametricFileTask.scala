package eu.man

package phevos

package dx

package engine

package domain

package tasks

import javax.xml.bind.annotation.XmlType
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import org.scala_tools.subcut.inject.BindingModule
import com.ibm.haploid.dx.engine.event.TaskCreate
import com.ibm.haploid.dx.engine.domain.{ TaskCollectionFSM, DomainObjectState, TaskData, TaskResultDetail, TaskDetail }
import com.ibm.haploid.core.service.Success
import dx.gep.{ PartMetadataInput, CATPartFileResult }
import dx.util.interfaces.{ PartInfo }
import eu.man.phevos.dx.engine.domain.operating.util.ZipFile
import eu.man.phevos.dx.engine.domain.operating.catia5.ParametricData

/**
 * Details Phevos DX Parametric Task.
 */
@XmlType(name = "parametric-file-task-detail")
case class ParametricFileTaskDetail(
  @xmlElement(required = true) partInfo: PartInfo)

  extends TaskDetail {

  private def this() = this(null)

  val name = "parametric-file-task"

}

case class ParametricFileTaskFSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskCollectionFSM {

  /**
   * Define task states
   */
  sealed trait ParametricTaskState extends DomainObjectState
  case object WaitingForParametricFileExtraction extends ParametricTaskState
  case object WaitingForParametricPreparation extends ParametricTaskState
  case object WaitingForParametricIntegration extends ParametricTaskState

  /**
   * Define task data classes
   */
  sealed abstract class ParametricTaskData extends TaskData
  case class ParametricTaskDetails(partinfo: PartInfo) extends ParametricTaskData

  /**
   * Static operators
   */
  val (parametricExtractionOp, parametricPreparationOp, parametricIntegrationOp) = task.detail match {
    case ParametricFileTaskDetail(partinfo) => (
      create(ParametricExtractionTaskDetail(partinfo), "parametric-extraction-task"),
      create(ParametricPreparationTaskDetail(partinfo), "parametric-preparation-task"),
      create(ParametricIntegrationTaskDetail(partinfo), "parametric-integration-task"))
  }

  start {
    case ParametricFileTaskDetail(partinfo) =>
      parametricExtractionOp.execute((), WaitingForParametricFileExtraction, ParametricTaskDetails(partinfo))
  }

  succeeded(WaitingForParametricFileExtraction) {
    case (parametricdata: ParametricData, _) =>
      parametricPreparationOp.execute(parametricdata, WaitingForParametricPreparation)
    case ((), _) =>
      finalize(TaskResultDetail(Success()))
  }

  succeeded(WaitingForParametricPreparation) {
    case (parametriczip: ZipFile, _) =>
      parametricIntegrationOp.execute(parametriczip, WaitingForParametricIntegration)
  }

  finalize(WaitingForParametricIntegration) {
    case ((), _) =>
      TaskResultDetail(Success())
  }

}