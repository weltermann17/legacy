package com.ibm.haploid

package dx

package engine

package domain

package flow

import javax.xml.bind.annotation.{ XmlType, XmlRootElement }

import akka.actor.actorRef2Scala
import akka.actor.ActorRef

import com.ibm.haploid.dx.engine.domain.Idle
import com.ibm.haploid.dx.engine.event.{ ResetAck, Reset, ReceiverEvent, PersistentEvent, Execute, DefaultExecutionResult }

trait Executable

  extends FlowBase {

  private[this] var _input: Option[Any] = None

  private[this] var _executed: Boolean = false

  protected[this] val parent: Option[ActorRef]

  private[this] var executablesForceAcks: Int = -1

  override def ignore: StateFunction = {
    val myHandlers: StateFunction = {
      case Event(ExecutionStarted, _) ⇒
        sender ! this._executed
        stay

      case Event(Reset(force), CompletedData(_, _, _)) ⇒
        reset(force)

      case Event(Reset(force), data) if (data.equals(initialData)) ⇒
        reset(force)

      case Event(Reset(force), IncompleteData(_, _)) ⇒
        if (force) {
          reset(force)
        } else
          stay

      case Event(ResetAck(force), _) if this.executablesForceAcks > -1 ⇒
        this match {
          case me: Executor[_, _] ⇒
            this.executablesForceAcks = this.executablesForceAcks - 1
            if (this.executablesForceAcks == 0) {
              journal ! ReceiverEvent(parent.get, ResetAck(force))
              this.executablesForceAcks = -1
              this._executed = false
              goto(Idle) using initialData
            } else stay
          case _ ⇒
            stay
        }
    }

    myHandlers orElse super.ignore
  }

  private[this] def reset(force: Boolean) = {
    this match {
      case me: Executor[_, _] ⇒
        me.executables.foreach(_.reset(force))
        this.executablesForceAcks = me.executables.size
        stay
      case _ if (isOnline) ⇒
        if (parent.isDefined)
          journal ! ReceiverEvent(parent.get, ResetAck(force))

        this._executed = false
        goto(Idle) using initialData
      case _ ⇒
        stay
    }
  }

  protected[this] def execute(f: Any ⇒ State) {
    when(Idle) {
      case event @ Event(exe @ Execute(input), _) ⇒
        this._input = Some(input)
        val state = f(input)

        this._executed = true

        state.stateData match {
          case IncompleteData(execution, null) ⇒
            goto(state.stateName) using IncompleteData(execution, input)
          case _ ⇒
            state
        }
    }
  }

  protected[this] def relaunchWithInput(f: PartialFunction[(Option[PersistentEvent], Any), State]) {
    relaunch {
      case event if (this._input.isDefined && f.isDefinedAt(event, this._input.get)) ⇒
        f(event, this._input.get)
      case _ ⇒
        stay
    }
  }

  protected[this] def complete[E <: PersistentEvent](f: PartialFunction[ExecutionResultDetail, E]) {
    onTransition {
      case _ -> Succeeded ⇒ nextStateData match {
        case data @ CompletedData(_, _, _) ⇒
          if (isOnline && parent.isDefined) journal ! ReceiverEvent(parent.get, f(data.details))
        case data ⇒
          log.error(data.toString())
          throw new Exception("StateData must be CompletedData in state Succeded.")
      }

      case _ -> Failed ⇒ nextStateData match {
        case data @ CompletedData(_, _, _) ⇒
          if (isOnline && parent.isDefined) {
            if (f.isDefinedAt(data.details))
              journal ! ReceiverEvent(parent.get, f(data.details))
            else
              journal ! ReceiverEvent(parent.get, DefaultExecutionResult(data.details.result))
          }
        case _ ⇒
          throw new Exception("StateData must be CompletedData in state Failed.")
      }
    }
  }

  when(Idle) {
    case Event(Reset(force), _) ⇒
      if (isOnline && parent.isDefined) {
        journal ! ReceiverEvent(parent.get, ResetAck(force))
      }

      stay
  }
}