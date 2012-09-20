package eu.man.phevos.dx.engine.domain.tasks
import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.core.service.Failure
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.DomainObjectState
import com.ibm.haploid.dx.engine.domain.TaskData
import com.ibm.haploid.dx.engine.domain.TaskDetail
import com.ibm.haploid.dx.engine.domain.TaskFSM
import com.ibm.haploid.dx.engine.domain.TaskResultDetail
import com.ibm.haploid.dx.engine.event.TaskCreate

import eu.man.phevos.dx.engine.domain.exceptions._
import eu.man.phevos.dx.engine.domain.operating.kvs.CheckMTBPartIndexIsSupercededOperationDetail
import eu.man.phevos.dx.engine.domain.operating.kvs.CheckPartNumberExistsOperationDetail
import eu.man.phevos.dx.engine.domain.operating.kvs.ValidateKStandOperationDetail
import eu.man.phevos.dx.util.interfaces.PartInfo
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlType(name = "check-task-detail")
case class CheckTaskDetail(

  @XmlElement(required = true) partinfo: PartInfo)

  extends TaskDetail {

  def this() = this(null)

  val name = "check-task"

}

case class CheckTaskFSM(task: TaskCreate)(implicit bindingmodule: BindingModule)

  extends TaskFSM {

  sealed trait CheckTaskState extends DomainObjectState
  case object WaitingForCheckPartNumberExists extends CheckTaskState
  case object WaitingForValidateKStand extends CheckTaskState
  case object WaitingForMTBIndexSuperceded extends CheckTaskState

  sealed trait CheckTaskData extends TaskData
  case class CheckTaskDetails(partinfo: PartInfo) extends CheckTaskData

  val (checkPartNumExistsOp, validateKStandOp, checkMTBIndexSupercededOp) = {
    task.detail match {
      case CheckTaskDetail(partinfo) ⇒ (
        create(CheckPartNumberExistsOperationDetail(partinfo), "check-partnumber-exists"),
        create(ValidateKStandOperationDetail(partinfo), "validate-kstand"),
        create(CheckMTBPartIndexIsSupercededOperationDetail(partinfo), "check-partnumber-is-superceded"))

    }
  }

  start {
    case (CheckTaskDetail(partinfo)) if (partinfo.mtbStandardPart) ⇒
      finalize(TaskResultDetail(Success(Failure(P0B1_StandardPart))))
    case (CheckTaskDetail(partinfo)) ⇒
      checkPartNumExistsOp.execute((), WaitingForCheckPartNumberExists, CheckTaskDetails(partinfo))
  }

  succeeded(WaitingForCheckPartNumberExists) {
    case (true, _) ⇒
      validateKStandOp.execute((), WaitingForValidateKStand)
    case (false, _) ⇒
      finalize(TaskResultDetail(Success(Failure(P0B2_PartnumberDoesNotExist))))
  }

  succeeded(WaitingForValidateKStand) {
    case (true, _) ⇒
      checkMTBIndexSupercededOp.execute((), WaitingForMTBIndexSuperceded)
    case (false, _) ⇒
      finalize(TaskResultDetail(Success(Failure(P0B3_KStandNotValid))))
  }

  finalize(WaitingForMTBIndexSuperceded) {
    case (false, _) =>
      TaskResultDetail(Success(Success(())))
    case (true, _) =>
      TaskResultDetail(Success(Failure(P0B4_Superceded)))
  }

}