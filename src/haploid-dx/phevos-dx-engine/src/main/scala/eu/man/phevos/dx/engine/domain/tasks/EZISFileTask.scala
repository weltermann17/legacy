package eu.man.phevos.dx.engine.domain.tasks
import com.ibm.haploid.dx.engine.domain.TaskDetail
import eu.man.phevos.dx.util.interfaces.PartInfo
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import javax.xml.bind.annotation.XmlType
import org.scala_tools.subcut.inject.BindingModule
import com.ibm.haploid.dx.engine.event.TaskCreate
import com.ibm.haploid.dx.engine.domain.TaskCollectionFSM
import com.ibm.haploid.dx.engine.domain.DomainObjectState
import com.ibm.haploid.dx.engine.domain.TaskData
import eu.man.phevos.dx.util.interfaces.MTBPartFile
import com.ibm.haploid.dx.engine.domain.TaskResultDetail
import com.ibm.haploid.core.service.Success

/**
 * Details Phevos DX Tiff Extraction Task.
 */
@XmlType(name = "ezis-task-detail")
case class EZISFileTaskDetail(
  @xmlElement(required = true) partInfo: PartInfo)

  extends TaskDetail {

  private def this() = this(null)

  val name = "ezis-file-task"

}

case class EZISFileTaskFSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskCollectionFSM {

  /**
   * Define task states
   */
  sealed trait EZISTaskState extends DomainObjectState
  case object WaitingForEZISFileExtraction extends EZISTaskState
  case object WaitingForEZISFilePreperation extends EZISTaskState
  case object WaitingForEZISFileIntegration extends EZISTaskState

  /**
   * Define task data classes
   */
  sealed abstract class EZISTaskData extends TaskData
  case class EZISTaskDetails(partinfo: PartInfo) extends EZISTaskData
  case class EZISFilePreperationData(partInfo: PartInfo, downloadedTiffs: List[MTBPartFile], preparedTiffs: List[MTBPartFile]) extends EZISTaskData // TODO: Add Failure counts?

  /**
   * Static operators
   */
  val (ezisExtractionOp, ezisIntegrationOp) = task.detail match {
    case EZISFileTaskDetail(partinfo) ⇒ (
      create(EZISFileExtractionTaskDetail(partinfo), "p1b-ezis-file-extraction"),
      create(EZISFileIntegrationTaskDetail(partinfo), "p3b-ezis-file-integration"))
  }

  start {
    case EZISFileTaskDetail(partinfo) ⇒
      ezisExtractionOp.execute((), WaitingForEZISFileExtraction, EZISTaskDetails(partinfo))
  }

  succeeded(WaitingForEZISFileExtraction) {
    case (list: List[_], EZISTaskDetails(partinfo)) if (list.size == 0) =>
      ezisIntegrationOp.execute(List[MTBPartFile](), WaitingForEZISFileIntegration)
      
    case (_files: List[_], EZISTaskDetails(partinfo)) =>
      val files = _files.map(_.asInstanceOf[MTBPartFile]).toList
      log.info(files.size + " Tiffs extracted from EZIS. Start to prepare ...")

      val efPrprtnTskDtl = EZISFilePreperationTaskDetail(partinfo)

      files.zipWithIndex.foreach {
        case (file, i) ⇒
          val preperation = create(efPrprtnTskDtl, "p2b-ezis-file-preperation-task-" + (i + 1))
          preperation.execute(file, WaitingForEZISFilePreperation)
      }

      goto(WaitingForEZISFilePreperation) using EZISFilePreperationData(partinfo, files, List[MTBPartFile]())
  }

  succeeded(WaitingForEZISFilePreperation) {
    case (file: MTBPartFile, EZISFilePreperationData(partinfo, ezisFiles, preparedFiles)) ⇒
      val details = EZISFilePreperationData(partinfo, ezisFiles, file :: preparedFiles)

      if (details.preparedTiffs.size < ezisFiles.size) {
        stay using details
      } else {
        ezisIntegrationOp.execute(details.preparedTiffs, WaitingForEZISFileIntegration)
      }
  }

  finalize(WaitingForEZISFileIntegration) {
    case ((), _) ⇒
      TaskResultDetail(Success(()))
  }

}