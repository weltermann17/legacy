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
import javax.xml.bind.annotation._
import java.nio.file.Path
import dx.ezis.EzisServices._
import dx.util.interfaces.{ PartInfo }
import dx.gep.{ PartMetadataInput, PartMetadata, CATPartFileResult }
import dx.engine.domain.operating.kvs.GetKVSFilenameForDMUOperationDetail
import dx.engine.domain.operating.catia5.{ CreateDMUFromPartCATScriptOperationDetail, DMUInput, DMUOutput }
import dx.engine.domain.operating.util.{ CreateTarOperationDetail, TarInput, TarFile }

/**
 * Details Phevos DX DMU Extraction Task.
 */
@XmlType(name = "dmu-preparation-task-detail")
case class DMUPreparationTaskDetail(
  @xmlElement(required = true) partinput: PartInfo)

  extends TaskDetail {

  private def this() = this(null)

  val name = "dmu-preparation-task"

}

/**
 *
 */
case class DMUPreparationTaskFSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskFSM {

  /**
   * Define task states
   */
  sealed trait DMUPreparationTaskState extends DomainObjectState
  case object WaitingFor_GetKVSFileName extends DMUPreparationTaskState
  case object WaitingFor_CreateDMUFromPart extends DMUPreparationTaskState
  case object WaitingFor_CreateKVSTar extends DMUPreparationTaskState

  /**
   * Define task data classes
   */
  sealed abstract class DMUPreparationTaskData extends TaskData
  case class DMUPreparationTaskDetails(partinfo: PartInfo, catpart: CATPartFileResult) extends DMUPreparationTaskData

  /**
   * Create operations
   */
  val (getKVSFileName_Operation, createDMUFromPart_Operation, createKVSTar_Operation) = {
    task.detail match {
      case DMUPreparationTaskDetail(partinput) ⇒ (
        create(new GetKVSFilenameForDMUOperationDetail(partinput), "get-kvs-filename-for-dmu"),
        create(new CreateDMUFromPartCATScriptOperationDetail(), "createdmufrompart"),
        create(new CreateTarOperationDetail(false), "create-kvs-tar"))
    }
  }

  /**
   * Define entry point
   */
  startWithInput {
    case (DMUPreparationTaskDetail(partinput), catpart: CATPartFileResult) ⇒
      getKVSFileName_Operation.execute(partinput, WaitingFor_GetKVSFileName, DMUPreparationTaskDetails(partinput, catpart))
  }

  /**
   * Define success cases if operations
   */
  succeeded(WaitingFor_GetKVSFileName) {
    case (kvsfilename: String, DMUPreparationTaskDetails(partinfo: PartInfo, catpart: CATPartFileResult)) ⇒
      createDMUFromPart_Operation.execute(DMUInput(catpart.path.toAbsolutePath, kvsfilename), WaitingFor_CreateDMUFromPart)
  }

  succeeded(WaitingFor_CreateDMUFromPart) {
    case (dmufile: DMUOutput, _) ⇒
      val newfilename = {
        dmufile.partfile.getFileName.toString.substring(0, dmufile.partfile.getFileName.toString.lastIndexOf(".")) + ".tar"
      }
      createKVSTar_Operation.execute(TarInput(dmufile.partfile, newfilename, "en2k@mtb_man"), WaitingFor_CreateKVSTar)
  }

  /**
   * Finalize Task
   */
  finalize(WaitingFor_CreateKVSTar) {
    case (result: TarFile, _) ⇒ {
      TaskResultDetail(Success(result))
    }
  }

}

