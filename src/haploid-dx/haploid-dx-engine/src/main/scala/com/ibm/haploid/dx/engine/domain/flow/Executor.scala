package com.ibm.haploid

package dx

package engine

package domain

package flow

import com.ibm.haploid.core.service.{ Success, Failure, Result }
import com.ibm.haploid.dx.engine.domain.{ Idle, DomainObjectState }
import com.ibm.haploid.dx.engine.event.{ ReceiverEvent, Exists, ExecutionStartEvent, ExecutionResult, ExecutionCreate, Execute, Collect, Reset }

import akka.actor.{ actorRef2Scala, ActorRef }
import akka.pattern.ask

trait Executor[ED <: ExecutionDetail, ERD <: ExecutionResultDetail]

  extends FlowBase {

  private var isPreStart = true

  val defaultPath: String

  protected[this] val executionsActorRef: ActorRef

  protected[this] def createEvent(name: String, details: ED): ExecutionCreate

  private[this] var executerFailure: Option[PartialFunction[(Throwable, ExecutionData), State]] = None

  private[this] val stateExceptionHandlers = scala.collection.mutable.Map[DomainObjectState, PartialFunction[(Throwable, ExecutionData), State]]()

  private[this] val finalizeExceptionHandlers = scala.collection.mutable.Map[DomainObjectState, PartialFunction[(Throwable, ExecutionData), ERD]]()

  private[this] var stateHistory: (StateHistory, Option[StateHistory]) = (StateHistory(initialState), None)

  private[this] def currentStateHistory = stateHistory._1

  private[flow] var executables = List[Execution]()

  object CollectResult {

    def apply(element: Any, depth: Option[Int]): CollectResult = new CollectResult(element, depth match {
      case Some(i) if i <= 1 ⇒ 0
      case _ ⇒ 1
    }, Some(1))

  }

  case class StateHistory(state: DomainObjectState) {

    var createdExecutions: List[Execution] = List()
    var sentExecutes: List[ReceiverEvent] = List()

  }

  case class Execution(id: String, detail: ED, pathPrefix: String) {

    val path = pathPrefix + "/" + id

    def execute(input: Any, state: DomainObjectState, data: Option[ExecutionData] = None): State = {
      val receiverEvent = ReceiverEvent(actorFor(path), Execute(input))

      if (isOnline)
        journal ! receiverEvent

      currentStateHistory.sentExecutes ++= List(receiverEvent)

      if (data == None)
        goto(state)
      else
        goto(state) using data.get
    }

    def execute(input: Any, state: DomainObjectState, data: ExecutionData): State = {
      this.execute(input, state, Some(data))
    }

    def execute(state: DomainObjectState): State = execute((), state, None)

    def execute(state: DomainObjectState, data: ExecutionData): State = execute((), state, Some(data))

    def reset(force: Boolean = false) {
      if (isOnline)
        journal ! ReceiverEvent(actorFor(path), Reset(force))
    }

    def init: Unit = init(false)

    def init(withExist: Boolean = false): Unit = {
      if (isOnline) {
        if (!withExist)
          journal ! ReceiverEvent(executionsActorRef, createEvent(id, detail))
        else {
          (context.actorFor(path) ? Exists) onFailure {
            case e: Throwable ⇒
              journal ! ReceiverEvent(executionsActorRef, createEvent(id, detail))
          }
        }
      }
    }
  }

  protected[this] def create(detail: ED, id: Option[String] = None, path: String = defaultPath): Execution = {
    executables = executables ++ List({
      if (id == None)
        Execution((executables.size + 1).toString(), detail, path)
      else
        Execution(id.get, detail, path)
    })

    currentStateHistory.createdExecutions ++= List(executables.last)
    if (!isPreStart) executables.last.init
    executables.last
  }

  protected[this] def create(detail: ED, id: String, path: String): Execution = {
    create(detail, Some(id), path)
  }

  protected[this] def create(detail: ED, id: String): Execution = {
    create(detail, Some(id))
  }

  override def preStart = {
    super.preStart

    executables.foreach(_.init)
    isPreStart = false
  }

  protected[this] def start(f: PartialFunction[ExecutionDetail, State]) {
    when(Idle) {
      case Event(ExecutionStartEvent(), _) ⇒
        f.apply(execution.detail)
    }
  }

  protected[this] def startWithInput(f: PartialFunction[(ExecutionDetail, Any), State]) {
    when(Idle) {
      case Event(ExecutionStartEvent(), IncompleteData(_, input)) ⇒
        f.apply(execution.detail, input)
    }
  }

  case class SucceededStatement(state: DomainObjectState) {
    def failed(ef: PartialFunction[(Throwable, ExecutionData), State]): Unit = {
      stateExceptionHandlers.put(state, ef)
    }
  }

  case class FinalizeStatement(state: DomainObjectState) {
    def failed(ef: PartialFunction[(Throwable, ExecutionData), ERD]): Unit = {
      finalizeExceptionHandlers.put(state, ef)
    }
  }

  protected[this] def succeeded(state: DomainObjectState)(f: PartialFunction[(Any, ExecutionData), State]): SucceededStatement = {
    succeeded_intern(state) {
      case (Success(result), data, event) if f.isDefinedAt(result, data) ⇒
        val newState = f(result, data)

        if (newState.stateName.eq(Succeeded) && !newState.stateData.isInstanceOf[CompletedData])
          throw new Exception("Use finalize to go into Succeeded State.")

        newState

      case (Failure(e), data, event) if (stateExceptionHandlers.isDefinedAt(state) && stateExceptionHandlers.get(state).get.isDefinedAt(e, data)) ⇒
        val newState = stateExceptionHandlers.get(state).get(e, data)

        if (newState.stateName.eq(Succeeded) && !newState.stateData.isInstanceOf[CompletedData])
          throw new Exception("Use finalize to go into Succeeded State.")

        newState

      case (Failure(e), data, event) if (this.executerFailure != None && this.executerFailure.get.isDefinedAt(e, data)) ⇒
        this.executerFailure.get(e, data)
    }
  }

  private[this] def succeeded_intern(state: DomainObjectState)(f: PartialFunction[(Result[Any], ExecutionData, ExecutionResult), State]): SucceededStatement = {
    when(state)(ignore)

    when(state) {
      case Event(event @ ExecutionResult(result), data) ⇒ result match {
        case result if f.isDefinedAt(result, data, event) ⇒
          f(result, data, event)

        case (Success(result)) ⇒
          val failure = Failure(new Exception("Unexpected Result in state " + state + " (" + result.toString() + ", " + data.toString() + ")"))

          if (f.isDefinedAt(failure, data, event))
            f(failure, data, event)
          else
            goto(Failed) using CompletedData(execution, event.created, DefaultExecutionResultDetail(failure))

        case (Failure(e)) ⇒
          goto(Failed) using CompletedData(execution, event.created, DefaultExecutionResultDetail(Failure(e)))

        case _ ⇒
          throw new Exception("Unexpected Exception in Flow.scala (succeeded_intern)")
      }
    }

    SucceededStatement(state)
  }

  protected[this] def finalize(state: DomainObjectState)(f: PartialFunction[(Any, ExecutionData), ERD]): FinalizeStatement = {
    val t = succeeded_intern(state) {
      case (Success(result), data, event) if f.isDefinedAt(result, data) ⇒
        finalize(f(result, data), event.created)

      case (Failure(e), data, event) if (finalizeExceptionHandlers.isDefinedAt(state) && finalizeExceptionHandlers.get(state).get.isDefinedAt(e, data)) ⇒
        finalize(finalizeExceptionHandlers.get(state).get(e, data), event.created)
    }

    FinalizeStatement(t.state)
  }

  protected[this] def failure(f: PartialFunction[(Throwable, ExecutionData), State]): Unit = {
    this.executerFailure = Some(f)
  }

  protected[this] def finalize(detail: ERD, time: Long = 0): State = {
    goto(
      detail.result match {
        case Success(_) ⇒
          Succeeded
        case Failure(e) ⇒
          Failed
      }) using CompletedData(execution, time, detail)
  }

  private[this] def redo(stateHistory: StateHistory) = {
    stateHistory.createdExecutions.foreach(_.init(true))
    stateHistory.sentExecutes.foreach { receiverevent ⇒
      actorFor(receiverevent.receiver.toString) ? ExecutionStarted onSuccess {
        case false ⇒
          journal ! receiverevent
      } onFailure {
        case _ ⇒
          journal ! receiverevent
      }
    }
  }

  def collect(collector: ActorRef, depth: Option[Int], any: Any) = {
    collector ! CollectResult(any, depth)

    if (depth.isDefined && depth.get > 1)
      executionsActorRef ! Collect(collector, Some(depth.get - 1))
    else if (!depth.isDefined)
      executionsActorRef ! Collect(collector, None)

    stay
  }

  onRelaunch {
    case event ⇒

      log.info("Relaunch " + self.path)

      this.stateHistory match {
        case (currentStateHistory, Some(lastStateHistory)) ⇒
          redo(lastStateHistory)
          redo(currentStateHistory)
        case (currentStateHistory, None) ⇒
          redo(currentStateHistory)
      }
  }

  onTransition {
    case x -> y ⇒
      this.stateHistory = this.stateHistory match {
        case (currentStateHistory, _) ⇒
          (StateHistory(y), Some(currentStateHistory))
      }
  }

}