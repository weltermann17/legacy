package eu.man.phevos

package dx

package scheduler

import java.util.concurrent.atomic.AtomicLong

import com.ibm.haploid.rest.HaploidService

class SchedulerMonitor extends HaploidService {

  import SchedulerMonitor._

  lazy val service = path("scheduler") { completeWith("Hello, I'm the scheduler " + monitorcounter.incrementAndGet) }

}

object SchedulerMonitor {

  val monitorcounter = new AtomicLong

}