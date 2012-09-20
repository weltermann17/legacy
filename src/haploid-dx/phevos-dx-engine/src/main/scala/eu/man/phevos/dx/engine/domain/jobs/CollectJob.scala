package eu.man

package phevos

package dx

package engine

package domain

package jobs

import akka.util.duration.{ longToDurationLong, intToDurationInt }

import com.ibm.haploid.core.concurrent.{ actorsystem ⇒ system }
import com.ibm.haploid.core.inject.BindingModule
import com.ibm.haploid.dx.engine.domain.{ JobSequenceTaskDetail, JobFSM ⇒ BaseJobFSM, Active }
import com.ibm.haploid.dx.engine.event.{ ResetAck, JobCreate }

import crt.CrtServices.EntitledParts
import eu.man.phevos.dx.engine.domain.tasks.CollectTaskDetail
import eu.man.phevos.dx.util.interfaces.MTBPartIndexOrdering

case class CollectJobFSM(job: JobCreate)(implicit bindingmodule: BindingModule)

  extends BaseJobFSM {

  case object CollectJobEvent

  val collectTask = create(CollectTaskDetail(true), "collect-task")

  val seqTaskDetail = JobSequenceTaskDetail {
    case (JobCreate(JobDetail(p1), _), JobCreate(JobDetail(p2), _)) ⇒
      if (p1.dxStatus.toInt != p2.dxStatus.toInt)
        p1.dxStatus.toInt - p2.dxStatus.toInt
      else
        MTBPartIndexOrdering(p1.mtbPartIndex).compare(p2.mtbPartIndex)
  }

  start {
    case _ ⇒
      system.scheduler.schedule(1 seconds, collectJobTimeout.toLong milliseconds, self, CollectJobEvent)
      goto(Active)
  }

  when(Active) {
    case Event(CollectJobEvent, _) ⇒
      collectTask.reset(true)
      stay
    case Event(ResetAck(_), _) ⇒
      log.debug("Getting Parts from CRT using EntitledDXStatus.")
      collectTask.execute((), Active)
  }

  succeeded(Active) {
    case (EntitledParts(parts), _) ⇒
      log.info("# of DX jobs found in CRT unload file : " + parts.size)

      val vwPartNumbers = parts.foldLeft(List[String]()) {
        case (list, partinfo) ⇒
          if (partinfo.dxStatus.toInt == 3 && !list.contains(partinfo.vwPartNumber))
            partinfo.vwPartNumber :: list
          else
            list
      }

      ((parts.filter { partinfo ⇒
        partinfo.dxStatus.toInt == 4 && !vwPartNumbers.contains(partinfo.vwDefiningIdent)
      } map (partinfo ⇒ JobCreate(JobDetail(partinfo)))) :: (vwPartNumbers map { vwPartNumber ⇒
        parts.filter(_.vwDefiningIdent.equals(vwPartNumber)).map { partinfo ⇒
          JobCreate(JobDetail(partinfo))
        } toList
      } toList)).zipWithIndex.foreach {
        case (jobs @ (JobCreate(JobDetail(partinfo), _) :: tail), i) ⇒
          log.info("Starting a new job sequence : " + jobs)
          create(seqTaskDetail).execute(jobs, Active)
        case _ ⇒
      }

      stay

    case (e: Exception) ⇒
      stay

    case (_) ⇒
      stay
  }

}