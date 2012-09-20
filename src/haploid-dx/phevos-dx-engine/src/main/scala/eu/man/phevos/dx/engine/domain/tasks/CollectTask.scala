package eu.man

package phevos

package dx

package engine

package domain

package tasks

import javax.xml.bind.annotation.XmlType

import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.{ TaskResultDetail, TaskFSM, TaskDetail, DomainObjectState }
import com.ibm.haploid.dx.engine.domain.binding.xmlTransient
import com.ibm.haploid.dx.engine.event.TaskCreate

import crt.CrtServices.EntitledParts
import eu.man.phevos.dx.engine.domain.operating.crt.EntitledDxStatusOperationDetail

/**
 * Details Phevos DX Collect Task.
 */
@XmlType(name = "phevos-dx-task-collect-detail")
case class CollectTaskDetail(

  @xmlTransient foo: Boolean)

  extends TaskDetail {

  private def this() = this(false)

  val name = "collect"

}

class CollectTaskFSM(val task: TaskCreate)(implicit bindingmodule: BindingModule)

  extends TaskFSM {

  case object CollectTaskEvent

  sealed trait CollectTaskState extends DomainObjectState
  case object WaitingForParts extends CollectTaskState

  val entitledDxStatusOperation = create(new EntitledDxStatusOperationDetail(), "collect")

  start {
    case _ ⇒
      entitledDxStatusOperation.execute((), WaitingForParts)
  }

  finalize(WaitingForParts) {
    case (EntitledParts(parts), _) ⇒
      TaskResultDetail(Success(EntitledParts(parts)))
    case (e: Exception, _) ⇒
      TaskResultDetail(Success(EntitledParts(List())))
  }

}