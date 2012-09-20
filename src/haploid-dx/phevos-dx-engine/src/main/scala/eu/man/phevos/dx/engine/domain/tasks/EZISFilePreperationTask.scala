package eu.man

package phevos

package dx

package engine

package domain

package tasks

import java.sql.Timestamp

import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.{TaskResultDetail, TaskFSM, TaskDetail, TaskData, DomainObjectState}
import com.ibm.haploid.dx.engine.event.TaskCreate

import engine.domain.exceptions.P2B1_KNRelease
import engine.domain.operating.catia5.{ReframeTiffCATScriptOperationInput, ReframeTiffCATScriptOperationDetail}
import engine.domain.operating.kvs.{RenameTiffFileOperationDetail, GetPartNameOperationDetail, CheckTZELZKABPageDVExsistsOperationDetail}
import eu.man.phevos.dx.engine.domain.exceptions.fromResultToPhevosException
import eu.man.phevos.dx.kvs.defaultVWChangeNumber
import eu.man.phevos.dx.kvs.services.PartNameLanguage
import eu.man.phevos.dx.util.interfaces.{PartInfo, MTBPartFile}
import javax.xml.bind.annotation.XmlType

/**
 *
 */
@XmlType(name = "ezis-file-preperation-task-detail")
case class EZISFilePreperationTaskDetail(

  @xmlElement(required = true) partInfo: PartInfo)

  extends TaskDetail {

  def this() = this(null)

  val name = "ezis-file-preperation"

}

/**
 *
 */
case class EZISFilePreperationTaskFSM(task: TaskCreate)(implicit bindingmodule: BindingModule)

  extends TaskFSM {

  sealed trait EZISFilePreperationTaskState extends DomainObjectState
  case object WaitingForRenameTiffFile extends EZISFilePreperationTaskState
  case object WaitingForCheckDVForTZELZKABExists extends EZISFilePreperationTaskState
  case object WaitingForPartNames extends EZISFilePreperationTaskState
  case object WaitingForCATScript extends EZISFilePreperationTaskState

  sealed trait EZISFilePreperationTaskData extends TaskData
  case class TaskDetails(
    partInfo: PartInfo,
    input: MTBPartFile,
    egPartName: Option[String] = None)
    extends EZISFilePreperationTaskData

  val (renameTiffOperator, checkTZELZKABPageDVExistsOp,
    getEgPartName, getPgPartName,
    reframeTiffCATScriptOperator) = {
    task.detail match {
      case EZISFilePreperationTaskDetail(partinfo) ⇒ (
        create(RenameTiffFileOperationDetail(partinfo), "rename-tiff-file"),
        create(CheckTZELZKABPageDVExsistsOperationDetail(partinfo), "check-dv-for-tiff-exists"),
        create(GetPartNameOperationDetail(partinfo, PartNameLanguage.English), "get-english-partname"),
        create(GetPartNameOperationDetail(partinfo, PartNameLanguage.Portuguese), "get-portuguese-partname"),
        create(ReframeTiffCATScriptOperationDetail(partinfo.mtbPartNumber, partinfo.mtbPartIndex, partinfo.vwPartNumber), "reframe-tiff-cat-script"))
    }
  }

  private[this] def renameTiffWithKNCheck(partinfo: PartInfo, input: MTBPartFile) = {
    if (!partinfo.knRelease)
      renameTiffOperator.execute(input, WaitingForRenameTiffFile, TaskDetails(partinfo, input))
    else
      finalize(TaskResultDetail(P2B1_KNRelease))
  }

  startWithInput {
    case (EZISFilePreperationTaskDetail(partinfo), input: MTBPartFile) if (partinfo.titleblock) ⇒
      if (!partinfo.isCOP)
        renameTiffWithKNCheck(partinfo, input)
      else
        checkTZELZKABPageDVExistsOp.execute(WaitingForCheckDVForTZELZKABExists, TaskDetails(partinfo, input))

    case (EZISFilePreperationTaskDetail(partinfo), input: MTBPartFile) ⇒
      getEgPartName.execute((), WaitingForPartNames, TaskDetails(partinfo, input))
  }

  succeeded(WaitingForCheckDVForTZELZKABExists) {
    case (true, TaskDetails(partinfo, input, _)) ⇒
      renameTiffWithKNCheck(partinfo, input)

    case (false, TaskDetails(partinfo, input, _)) ⇒
      if (partinfo.mtbPartIndex.equals(input.metadata.mtbPartIndex))
        renameTiffWithKNCheck(partinfo, input)
      else
        getEgPartName.execute((), WaitingForPartNames)
  }

  succeeded(WaitingForPartNames) {
    case (english: String, TaskDetails(details, input, None)) ⇒
      getPgPartName.execute((), WaitingForPartNames, TaskDetails(details, input, Some(english)))

    case (pg: String, TaskDetails(partinfo, input, Some(eg))) ⇒
      val drawingdate = new Timestamp(
        partinfo.vwDrawingDate match {
          case None ⇒
            System.currentTimeMillis()
          case Some(l) ⇒
            l
        })

      val changeNumber =
        partinfo.vwChangeNumber match {
          case Some(s) if (s.length > 0) ⇒
            s
          case None ⇒
            defaultVWChangeNumber
        }

      reframeTiffCATScriptOperator.execute(ReframeTiffCATScriptOperationInput(input, pg, eg, drawingdate, changeNumber), WaitingForCATScript)
  }

  succeeded(WaitingForCATScript) {
    case (file: MTBPartFile, TaskDetails(details, input, _)) ⇒
      renameTiffOperator.execute(file, WaitingForRenameTiffFile)
  }

  finalize(WaitingForRenameTiffFile) {
    case (result: MTBPartFile, _) ⇒
      TaskResultDetail(Success(result))
  }

}