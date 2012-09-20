package eu.man

package phevos

package dx

package engine

package domain

package tasks

package experimental

import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.domain.operating.util.{ ScriptOperationDetail, MoveOperationDetail }
import com.ibm.haploid.dx.engine.domain.{ TaskFSM, TaskDetail, Idle, Active }
import com.ibm.haploid.dx.engine.event.{ TaskOperationResult, TaskCreate }
import com.ibm.haploid.dx.engine.defaulttimeout

import dx.util.interfaces.MTBPartFile

import akka.actor.actorRef2Scala
import akka.pattern.ask
import javax.xml.bind.annotation.XmlType
import operating.catia5.{ RunCATScriptOperationDetail, ReframeTiffCATScriptOperationDetail }

/**
 * Details of a Phevos DX Job.
 */
@XmlType(name = "phevos-dx-task-1-detail")
case class Task1Detail(
  @xmlAttribute(required = true) description: String)

  extends TaskDetail {

  private def this() = this(null)

  val name = "task"

}

/**
 *
 */
case class Task1FSM(

  task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskFSM {

  val moveOperation = create(MoveOperationDetail("abc", "def"), "move")

  //val tiffOperation = create(UnstampedTiffOperationDetail("33.77115-8558", "1", "_B_"), "unstampedtiff")

  //  val scriptOperation = create(ScriptOperationDetail(
  //    "test",
  //    List.empty,
  //    Map("testenvvar" -> "blabla", "env2" -> "xxxyyyzzz"),
  //    "env.txt",
  //    10000), "script")

  val runCatScriptOperation = create(RunCATScriptOperationDetail(
    "simpleruncatscript",
    "PhevosDX.catvba",
    "SimpleTest",
    List.empty,
    Map("CNEXTOUTPUT" -> "cnextlog.txt"),
    "cnextlog.txt",
    60 * 60 * 1000), "runcatscript")

  val reframeTiffOperation = create(ReframeTiffCATScriptOperationDetail(
    "36.71201-4070",
    "_A_",
    "T.EST.MHE.123.01"), "reframetiff")

  //  val uploadKVSOperation = create(UploadKVSOperationDetail("T.EST.MIW.MAN.05"), "uploadkvs")

  //  val dxStatusOperation = create(EntitledDxStatusOperationDetail(false))

  start {
    case _ ⇒
      //      moveOperation.execute((), Active)
      //      tiffOperation.execute((), Active)
      //      scriptOperation.execute((), Active)
      //      runCatScriptOperation.execute((), Active)
      reframeTiffOperation.execute(MTBPartFile(java.nio.file.Paths.get("C:\\tmp\\36712014070_01_A_CX_pre.tif")), Active)
    //      uploadKVSOperation.execute(TiffFile(java.nio.file.Paths.get("C:\\temp\\36712014070_01_A_CX_pre.tif")), Active)
    //      dxStatusOperation.execute((), Active)
  }

  when(Idle) {

    case Event(result @ TaskOperationResult(detail), _) ⇒
      log.debug(result.toString)
      stay

  }

  when(Active) {
    case Event(result @ TaskOperationResult(detail), _) ⇒
      log.debug(result.toString)
      stay
  }

}

