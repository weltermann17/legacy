package eu.man

package phevos

package dx

package engine

package domain

package tasks

package experimental

import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.dx.engine.domain.binding.xmlAttribute
import com.ibm.haploid.dx.engine.domain.{ TaskFSM, TaskDetail, Idle, DomainObjectState }
import com.ibm.haploid.dx.engine.event.{ TaskCreate, ReceiverEvent, OperationCreate, Execute }
import com.ibm.haploid.dx.engine.defaulttimeout
import akka.actor.actorRef2Scala
import akka.pattern.ask
import javax.xml.bind.annotation.XmlType
import operating.ezis.UnstampedTiffOperationDetail
import eu.man.phevos.dx.engine.domain.operating.ComplexOperationDetail
import com.ibm.haploid.dx.engine.domain.TaskData
import com.ibm.haploid.dx.engine.domain.flow.Succeeded
import com.ibm.haploid.dx.engine.domain.flow.DefaultExecutionResultDetail
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.TaskResultDetail

/**
 * Details Phevos DX Tiff Task.
 */
@XmlType(name = "phevos-dx-task-tiff-detail")
case class ExampleTaskDetail(
  @xmlAttribute(required = true) description: String)

  extends TaskDetail {

  private def this() = this(null)

  val name = "example"

}

/**
 *
 */
case class ExampleTaskFSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskFSM {

  /**
   * Define task states
   */
  sealed trait ExampleTaskState extends DomainObjectState
  case object WaitingForOperation_1 extends ExampleTaskState
  case object WaitingForOperation_2 extends ExampleTaskState

  /**
   * Define task data classes
   */
  sealed abstract class ExampleTaskData extends TaskData
  case class Result_1(sum: Int) extends ExampleTaskData

  /**
   * Create operations
   */
  val firstOperation = create(new ComplexOperationDetail(3))
  val secondOperation = create(new ComplexOperationDetail(4))

  /**
   * Define entry point
   */
  start {
    case ExampleTaskDetail(_) ⇒
      firstOperation.execute(2, WaitingForOperation_1)
  }

  /**
   * Define success cases if operations
   */
  succeeded(WaitingForOperation_1) {
    case (result: Int, _) ⇒
      val r1 = Result_1(result)
      log.info("3 + 2 = " + r1.sum)
      secondOperation.execute(result, WaitingForOperation_2, r1)
  }

  /**
   * The last success case should be handled with finalize, which returns
   * the result of this task.
   */
  finalize(WaitingForOperation_2) {
    case (result: Int, Result_1(firstSum)) ⇒
      val r = firstSum + " + 4 = " + result
      log.info(firstSum + " + 4 = " + result)
      TaskResultDetail(Success(r))
  }

}

