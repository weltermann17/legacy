package com.ibm.haploid

package dx

package engine

package domain

package flow

import com.ibm.haploid.core.newLogger
import com.ibm.haploid.core.service._
import com.ibm.haploid.dx.engine.domain._
import com.ibm.haploid.dx.engine.event._
import javax.xml.bind.annotation.adapters._
import javax.xml.bind.annotation._
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.journal._
import akka.actor.ActorPath

case object ExecutionStarted

case class OperationResultDetail(result: Result[Any])

trait ExecutionData
case class IncompleteData(create: ExecutionCreate, input: Any) extends ExecutionData
case class CompletedData(create: ExecutionCreate, completed: Long, details: ExecutionResultDetail) extends ExecutionData

trait ExecutionDetail extends DomainObject

trait ExecutionResultDetail extends DomainObject {

  @xmlTransient val result: Result[Any]

}

@XmlRootElement(name = "execution-result-detail")
@XmlType(propOrder = Array("success", "result"))
case class DefaultExecutionResultDetail(result: Result[Any]) extends ExecutionResultDetail {

  def this() = this(null)

  @XmlAttribute(required = true) def getSuccess = result match {
    case Success(_) ⇒ true
    case Failure(_) ⇒ false
  }

  @XmlJavaTypeAdapter(classOf[CDataAdapter]) def getResult = result.toString

}

sealed trait FlowState extends DomainObjectState
case object Succeeded extends FlowState
case object Failed extends FlowState

trait FlowBase

  extends DomainObjectFSM[ExecutionData] {

  implicit val selfPath: ActorPath = self.path

  private[flow] var _isOnline: Option[Boolean] = None

  private[this] var _onRelaunch: Option[PartialFunction[Option[PersistentEvent], State]] = None

  private[this] var _onRelaunchHandler: List[PartialFunction[Option[PersistentEvent], Unit]] = List()

  private[this] val _isContinious = continuousJobPaths.find(self.path.toString.startsWith(_)) match {
    case Some(_) ⇒ true
    case None ⇒ false
  }
  
  protected[flow] val logger = newLogger(this)

  protected[flow] val initialState = Idle

  protected[this] val execution: ExecutionCreate

  protected[this] def isOnline: Boolean = if (_isOnline.isDefined) _isOnline.get else execution.online

  protected[this] lazy val initialData: ExecutionData = IncompleteData(execution, null)

  override def ignore: StateFunction = {
    val myHandlers: StateFunction = {
      case Event(RelaunchHerald, _) ⇒
        this._isOnline = Some(true)
        stay

      case Event(Relaunch(event), _) ⇒
        this._onRelaunchHandler.foreach { f ⇒
          if (f.isDefinedAt(event)) f(event)
        }

        if (this._onRelaunch.isDefined && this._onRelaunch.get.isDefinedAt(event)) {
          this._onRelaunch.get(event)
        } else stay
    }

    myHandlers orElse super.ignore
  }

  def relaunch(f: PartialFunction[Option[PersistentEvent], State]) {
    this._onRelaunch = Some(f)
  }

  def onRelaunch(f: PartialFunction[Option[PersistentEvent], Unit]) {
    this._onRelaunchHandler = List(f) ++ this._onRelaunchHandler
  }

  startWith(initialState, initialData)

  when(Succeeded)(ignore)

  when(Failed)(ignore)

}
