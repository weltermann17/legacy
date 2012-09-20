package eu.man

package phevos

package dx

package engine

package domain

import javax.xml.bind.annotation.XmlType
import akka.dispatch.Await
import akka.pattern.ask
import com.ibm.haploid.dx.engine.defaulttimeout
import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.dx.engine.domain.operating.util._
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.domain.{ TaskFSM, TaskDetail, Idle, Active }
import com.ibm.haploid.dx.engine.event.{ TaskCreate, ReceiverEvent, OperationCreate, Execute }
import eu.man.phevos.dx.engine.domain.operating.UnstampedTiffOperationDetail

/**
 * Details of a Phevos DX Job.
 */
@XmlType(name = "phevos-dx-task-1-detail")
case class Task1Detail(
  @xmlAttribute(required = true) name: String,
  @xmlAttribute(required = true) description: String)

  extends TaskDetail {

  private def this() = this(null, null)

}

/**
 *
 */
case class Task1FSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskFSM {

  override def preStart = {
    super.preStart
    if (task.online) {
      journal ? ReceiverEvent(operations, OperationCreate(task, "1", MoveOperationDetail("abc", "def")))
      journal ? ReceiverEvent(operations, OperationCreate(task, "2", new ScriptOperationDetail("test", 10000)))
      self ! "Online"
    }
  }

  when(Idle) {
    case Event("Online", _) ⇒
      actorFor("operations/1") ! Execute
      actorFor("operations/2") ! Execute
      goto(Active)
  }

}

/**
 * Details of a Phevos DX Job.
 */
@XmlType(name = "phevos-dx-task-2-detail")
case class Task2Detail(
  @xmlAttribute(required = true) name: String,
  @xmlAttribute(required = true) description: String)

  extends TaskDetail {

  private def this() = this(null, null)

}

/**
 *
 */
case class Task2FSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskFSM {

  override def preStart = {
    super.preStart
    if (task.online) {
      journal ? ReceiverEvent(operations, OperationCreate(task, "1", new UnstampedTiffOperationDetail("33.77115-8558", "1", "_B_")))
      self ! "Online"
    }
  }

  when(Idle) {
    case Event("Online", _) ⇒
      actorFor("operations/1") ! Execute
      goto(Active)
  }

}

