package eu.man

package phevos

package dx

package engine

package domain

import javax.xml.bind.annotation.{ XmlType, XmlElement, XmlAttribute }

import akka.actor._

import com.ibm.haploid.dx.engine.domain.{ JobDetail ⇒ BaseJobDetail, JobFSM ⇒ BaseJobFSM }
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.domain.test._
import com.ibm.haploid.dx.engine.event._
import com.ibm.haploid.core.inject.BindingModule

/**
 * Details of a DX job.
 */
@XmlType(name = "phevos-dx-job-detail")
case class JobDetail(
  @xmlAttribute(required = true) partnumber: String,
  @xmlAttribute(required = true) revision: String,
  @xmlAttribute partnerpartnumber: String,
  @xmlAttribute partnerrevision: String)

  extends BaseJobDetail {

  private def this() = this(null, null, null, null)

}

/**
 *
 */
case class JobFSM(

  job: JobCreate)(

    implicit bindingmodule: BindingModule)

  extends BaseJobFSM {

  override def preStart = {
    super.preStart
    if (job.online) {
      journal ! ReceiverEvent(tasks, TaskCreate(classOf[Task1FSM], job, "1", Task1Detail("1", "move and script test")))
      // journal ! ReceiverEvent(tasks, TaskCreate(classOf[Task2FSM], job, "2", Task2Detail("2", "TIFF Test Task")))
    }
  }

}
