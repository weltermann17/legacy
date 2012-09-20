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
import eu.man.phevos.dx.engine.domain.operating.util.TarFile

/**
 * Details Phevos DX DMU Task.
 */
@XmlType(name = "dmu-file-task-detail")
case class DMUFileTaskDetail(
  @xmlElement(required = true) partInfo: PartInfo)

  extends TaskDetail {

  private def this() = this(null)

  val name = "dmu-file-task"

}

case class DMUFileTaskFSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskCollectionFSM {

  /**
   * Define task states
   */
  sealed trait DMUTaskState extends DomainObjectState
  case object WaitingForDMUFileExtraction extends DMUTaskState
  case object WaitingForDMUPreparation extends DMUTaskState
  case object WaitingForDMUIntegration extends DMUTaskState

  /**
   * Define task data classes
   */
  sealed abstract class DMUTaskData extends TaskData
  case class DMUTaskDetails(partinfo: PartInfo) extends DMUTaskData

  /**
   * Static operators
   */
  val (dmuExtractionOp, dmuPreparationOp, dmuIntegrationOp) = task.detail match {
    case DMUFileTaskDetail(partinfo) ⇒ (
      create(DMUExtractionTaskDetail(PartMetadataInput(partinfo.mtbPartNumber, partinfo.mtbPartIndex)), "dmu-extraction-task"),
      create(DMUPreparationTaskDetail(partinfo), "dmu-preparation-task"),
      create(DMUIntegrationTaskDetail(partinfo), "dmu-integration-task"))
  }

  start {
    case DMUFileTaskDetail(partinfo) ⇒
      dmuExtractionOp.execute((), WaitingForDMUFileExtraction)
  }

  succeeded(WaitingForDMUFileExtraction) {
    case ((false, catpart: CATPartFileResult), _) ⇒
      dmuPreparationOp.execute(catpart, WaitingForDMUPreparation)
    case (true, _) ⇒
      finalize(TaskResultDetail(Success()))
  }

  succeeded(WaitingForDMUPreparation) {
    case (dmutar: TarFile, _) ⇒
      dmuIntegrationOp.execute(dmutar, WaitingForDMUIntegration)
  }

  finalize(WaitingForDMUIntegration) {
    case ((), _) ⇒
      TaskResultDetail(Success())
  }

}