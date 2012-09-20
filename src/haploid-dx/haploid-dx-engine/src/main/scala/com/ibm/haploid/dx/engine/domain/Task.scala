package com.ibm.haploid

package dx

package engine

package domain

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlElement, XmlAttribute }

import akka.actor.{ actorRef2Scala, Props }
import akka.pattern.ask

import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.core.service.{ Success, Result, Failure }
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.domain.flow.{ Executor, ExecutionResultDetail, ExecutionDetail, ExecutionData, Executable, DefaultExecutionResultDetail, CompletedData }
import com.ibm.haploid.dx.engine.domain.operating.OperationDetail
import com.ibm.haploid.dx.engine.event.{ TaskResult, TaskCreate, ReceiverEvent, OperationCreate, JobCreate, ExecutionStartEvent }

import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import event.Collect
import operating.{ OperationMonitorFSM, OperationMonitor }

/**
 * Details of a Task.
 */
abstract class TaskDetail extends ExecutionDetail {

  val name: String

}

/**
 * Data of a Task.
 */
trait TaskData extends ExecutionData

/**
 *
 */
@XmlRootElement(name = "task-result-detail")
@XmlType(propOrder = Array("success", "res"))
case class TaskResultDetail(@xmlTransient result: Result[Any])

  extends ExecutionResultDetail {

  def this() = this(null)

  @XmlAttribute(required = true) def getSuccess = result match {
    case Success(_) ⇒ true
    case Failure(_) ⇒ false
  }

  @XmlJavaTypeAdapter(classOf[CDataAdapter]) def getRes = result.toString

}

/**
 *
 */
@XmlRootElement(name = "task")
@XmlType(propOrder = Array("id", "job", "name", "status", "created", "result", "error", "detail", "operations", "tasks"))
case class Task(

  event: TaskCreate,

  @xmlAttribute(required = true) name: String,

  @xmlAttribute(required = true) status: String,

  @xmlElement result: TaskResultDetail,

  @xmlElement error: DefaultExecutionResultDetail)

  extends DomainObject {

  private def this() = this(null, null, null, null, null)

  @XmlAttribute(required = true) val id = event.id

  @XmlAttribute(required = true) val job = event.job.id

  @XmlAttribute(required = true) val created = event.created

  @XmlElement(required = true) val detail = event.detail

  @XmlElement def getOperations = if (operations.isInstanceOf[OperationMonitor]) operations else null

  @XmlElement def getTasks = if (!operations.isInstanceOf[OperationMonitor]) operations else null

  def add(om: Monitor[_]) = operations = om

  private[this] var operations: Monitor[_] = null

}

/**
 *
 */
trait BaseTaskFSM[ED <: ExecutionDetail]

  extends Executor[ED, TaskResultDetail]

  with Executable {

  protected[this] val task: TaskCreate

  protected[this] val execution = task

  protected[this] lazy val parent = Some(context.system.actorFor(self.path.parent.parent))

  override val log = core.newLogger(task.toString) 
    
  execute { input: Any ⇒
    if (isOnline)
      journal ! ReceiverEvent(self, ExecutionStartEvent())

    stay
  }

  complete {
    case TaskResultDetail(result) ⇒
      TaskResult(result)
  }

  whenUnhandled {
    case Event(Collect(collector, depth), CompletedData(_, _, detail: DefaultExecutionResultDetail)) ⇒
      collect(collector, depth, Task(task, self.path.name, stateName.toString.toLowerCase, null, detail))
    case Event(Collect(collector, depth), CompletedData(_, _, detail: TaskResultDetail)) ⇒
      collect(collector, depth, Task(task, self.path.name, stateName.toString.toLowerCase, detail, null))
    case Event(Collect(collector, depth), _) ⇒
      collect(collector, depth, Task(task, self.path.name, stateName.toString.toLowerCase, null, null))
  }

  initialize

}

/**
 *
 */
trait TaskFSM

  extends BaseTaskFSM[OperationDetail] {

  override val defaultPath = "operations"

  protected[this] val executionsActorRef = context.actorOf(Props(new OperationMonitorFSM(task)), name = defaultPath)

  def createEvent(name: String, detail: OperationDetail) = OperationCreate(task, name, detail)

}

/**
 *
 */
trait TaskCollectionFSM

  extends BaseTaskFSM[TaskDetail] {

  override val defaultPath = "subtasks"

  protected[this] val executionsActorRef = context.actorOf(Props(new TaskMonitorFSM(task.job)), name = defaultPath)

  def createEvent(name: String, detail: TaskDetail) = TaskCreate(task.job, name, detail)

}

/**
 *
 */
@XmlType(name = "job-sequence-task-detail")
case class JobSequenceTaskDetail(

  @xmlTransient compare: (JobCreate, JobCreate) ⇒ Int)

  extends TaskDetail {

  def this() = this((_, _) ⇒ 0)

  val name = "job-sequence-task"

}

/**
 *
 */
class JobSequenceTaskFSM(

  val task: TaskCreate)(

    implicit bindingmodule: BindingModule)

  extends TaskFSM {

  case object JobOrdering extends Ordering[JobCreate] {

    def compare(me: JobCreate, that: JobCreate): Int = {
      task.detail match {
        case JobSequenceTaskDetail(compare) ⇒
          compare(me, that)
      }
    }

  }

  sealed trait SeqTaskState extends DomainObjectState
  case object WaitingForJob extends SeqTaskState

  sealed trait SeqTaskData extends TaskData
  case class SeqTaskDetails(parts: List[JobCreate], lastJobId: String) extends SeqTaskData

  startWithInput {
    case (_, list: List[_]) ⇒
      val jobs = list.map(_.asInstanceOf[JobCreate]).toList
      startJob(jobs.sortBy(s ⇒ s)(JobOrdering).map(job ⇒ JobCreate(job.detail, Some(self.path.toString))).toList)
  }

  succeeded(WaitingForJob) {
  	case (job @ JobCreate(_, _), SeqTaskDetails(jobs, lastJobId)) if job.id.equals(lastJobId) ⇒ startJob(jobs)
    case (job @ JobCreate(_, _), SeqTaskDetails(jobs, lastJobId)) ⇒ stay
  } failed {
    case (JobFailedException(job, _), SeqTaskDetails(jobs, lastJobId)) if job.id.equals(lastJobId) ⇒ startJob(jobs)
    case (JobFailedException(job, _), SeqTaskDetails(jobs, lastJobId)) ⇒ stay
    case (_, SeqTaskDetails(jobs, _)) ⇒ startJob(jobs)
  }

  private[this] def startJob(list: List[JobCreate]) = {
    list match {
      case (job :: tail) ⇒
        log.info("Start job " + job)
        if (task.online) journal ? ReceiverEvent("/engine/jobs", job)
        goto(WaitingForJob) using SeqTaskDetails(tail, job.id)
      case Nil ⇒
        finalize(TaskResultDetail(Success(())))
    }
  }

}

