package eu.man.phevos

package dx

package engine

package domain

package tasks

import javax.xml.bind.annotation.XmlType
import org.scala_tools.subcut.inject.BindingModule
import com.ibm.haploid.core.service.{ Success, Failure }
import com.ibm.haploid.core.service.SimpleServiceException
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.{ TaskResultDetail, TaskFSM, TaskDetail, DomainObjectState }
import com.ibm.haploid.dx.engine.event.TaskCreate
import dx.engine.domain.operating.catia5.ParametricData
import dx.gep.{ PartMetadataInput, NativeFormats }
import operating.catia5.ExtractLcaOperationDetail
import operating.gep.GetNativeFormatsDetail
import exceptions._
import eu.man.phevos.dx.util.interfaces.PartInfo

@XmlType(name = "parametric-extraction-task-detail")
case class ParametricExtractionTaskDetail(
    
  @xmlElement(required = true) partinfo: PartInfo) extends TaskDetail {

  private def this() = this(null)

  val name = "parametric-extraction-task"

}

case class ParametricExtractionTaskFSM(task: TaskCreate)(implicit bindingmodule: BindingModule) extends TaskFSM {

  /**
   * Define task states
   */
  sealed trait ParametricExtractionTaskState extends DomainObjectState
  case object WaitingForGetNativeFormats extends ParametricExtractionTaskState
  case object WaitingForExtractParametric extends ParametricExtractionTaskState

  /**
   * Create operations
   */
  val (getNativeFormatsOperation, extractParametricOperation) = {
    task.detail match {
      case ParametricExtractionTaskDetail(partinfo) ⇒ (
        create(new GetNativeFormatsDetail(partinfo), "getnativeformats"),
        create(new ExtractLcaOperationDetail(null, List.empty, Map.empty), "extractfromlca"))
    }
  }

  /**
   * Define entry point
   */
  start {
    case ParametricExtractionTaskDetail(partinput) ⇒
      getNativeFormatsOperation.execute((), WaitingForGetNativeFormats)
  }

  /**
   * Define success cases if operations
   */
  succeeded(WaitingForGetNativeFormats) {
    case (native: NativeFormats, _) ⇒
      if (!native.isReleased) {
        finalize(TaskResultDetail(Failure(P1A2_PartIndexNotReleased)))
      } else {
        extractParametricOperation.execute(native, WaitingForExtractParametric)
      }
  }

  /**
   * Finalize Task
   */
  finalize(WaitingForExtractParametric) {
    case (result: ParametricData, _) ⇒
      TaskResultDetail(Success(result))
  }
}