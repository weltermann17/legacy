package com.ibm.haploid

package dx

package engine

package domain

import java.nio.file.Paths
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import javax.xml.bind.annotation.{ XmlType, XmlRootElement, XmlElement, XmlAttribute }
import akka.actor.actorRef2Scala
import akka.actor.Props
import core.file.deleteDirectory
import core.service.{ Success, Result, Failure }
import flow.{ ExecutionResultDetail, ExecutionDetail, ExecutionData, Executable, CompletedData, Executor }
import event.{ TaskCreate, JobResult, JobCreate, Collect }
import binding._
import akka.actor.ActorRef

/**
 * States of a Job.
 */
sealed trait JobState extends DomainObjectState

case object JobSucceeded extends JobState
case object JobPartiallySucceeded extends JobState
case object JobFailed extends JobState
case object JobArchived extends JobState

/**
 * Details of a Job.
 */
@XmlType(name = "job-detail")
abstract class JobDetail extends ExecutionDetail {

  val name: String

}

@XmlType(name = "continuous-job-detail")
case class ContinuousJobDetail(

  @xmlAttribute(required = true) val name: String)

  extends JobDetail {

  def this() = this(null)

}

/**
 * Data of a Job.
 */
case class JobData(job: JobCreate) extends ExecutionData

case class JobFailedException(job: JobCreate, throwable: Throwable) extends Throwable {

  override def getMessage = throwable.getMessage

  override def toString = throwable.toString
}

/**
 *
 */
@XmlRootElement(name = "job-result-detail")
@XmlType(propOrder = Array("success", "result"))
case class JobResultDetail(result: Result[Unit])

  extends ExecutionResultDetail {

  def this() = this(null)

  @XmlAttribute(required = true) def getSuccess = result match {
    case Success(_) ⇒ true
    case Failure(_) ⇒ false
  }

  @XmlJavaTypeAdapter(classOf[CDataAdapter]) def getResult = result.toString

}

/**
 *
 */
@XmlRootElement(name = "job")
@XmlType(propOrder = Array("id", "counter", "status", "created", "result", "detail", "tasks"))
case class Job(

  event: JobCreate,

  @xmlAttribute(required = true) status: String,

  @xmlElement result: String)

  extends DomainObject {

  private def this() = this(null, null, null)

  @XmlAttribute(required = true) val id = event.id

  @XmlAttribute(required = true) val counter = event._counter

  @XmlAttribute(required = true) val created = event.created

  @XmlElement(required = true) val detail = event.detail

  @XmlElement def getTasks = tasks

  def add(tm: TaskMonitor) = tasks = tm

  private[this] var tasks: TaskMonitor = null

}

/**
 *
 */
trait JobFSM

  extends Executor[TaskDetail, JobResultDetail]

  with Executable {

  override val defaultPath = "tasks"

  val job: JobCreate

  private[this] var startFunction: Option[PartialFunction[ExecutionDetail, State]] = None

  private[this] var startWithInputFunction: Option[PartialFunction[(ExecutionDetail, Any), State]] = None

  protected[this] val execution = job

  protected[this] val executionsActorRef = context.actorOf(Props(new TaskMonitorFSM(job)), name = defaultPath)

  protected[this] lazy val parent = job.trigger match {
    case None ⇒
      None
    case Some(s) ⇒
      Some(context.system.actorFor(s))
  }

  private[this] lazy val basedirectory = {
    Paths.get(operating.rootdirectory.resolve(self.path.toString.replace("akka://default/user/", "")).toAbsolutePath.toString
      .replace(job.id, job._counter.toString))
  }

  override val log = core.newLogger(job.toString)

  def createEvent(name: String, detail: TaskDetail) = TaskCreate(job, name, detail)

  override protected[this] def start(f: PartialFunction[ExecutionDetail, State]) {
    this.startFunction = Some(f)
  }

  override protected[this] def startWithInput(f: PartialFunction[(ExecutionDetail, Any), State]) {
    log.warning("startWithInput makes no sense in a Job. There can't be any Input.")
    this.startWithInputFunction = Some(f)
  }

  override def preStart = {
    super.preStart
    val state = if (this.startFunction.isDefined)
      this.startFunction.get(this.execution.detail)
    else if (this.startWithInputFunction.isDefined)
      this.startWithInputFunction.get(this.execution.detail, null)
    else
      stay
    startWith(state.stateName, state.stateData)
  }

  complete {
    case JobResultDetail(Success(_)) ⇒
      if (removejobdirectoryonsuccess) {
        deleteDirectory(basedirectory.toFile)
        log.debug("Job successfully completed. Cleaned and removed directory : " + basedirectory)
      }
      JobResult(Success(job))
    case JobResultDetail(Failure(e)) ⇒
      if (removejobdirectoryonfailure) {
        deleteDirectory(basedirectory.toFile)
        log.debug("Job completed with failure. Cleaned and removed directory : " + basedirectory)
      }
      JobResult(Failure(JobFailedException(job, e)))
  }

  whenUnhandled {
    case Event(Collect(collector, depth), CompletedData(_, _, detail)) ⇒
      detail.result match {
        case Failure(e) ⇒
          collect(collector, depth, Job(job, stateName.toString.toLowerCase, e.getMessage))
        case _ ⇒
          collect(collector, depth, Job(job, stateName.toString.toLowerCase, null))
      }
    case Event(Collect(collector, depth), _) ⇒
      collect(collector, depth, Job(job, stateName.toString.toLowerCase, null))
  }

  initialize

}

