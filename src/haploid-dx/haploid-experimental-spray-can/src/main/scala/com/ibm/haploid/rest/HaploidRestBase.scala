package com.ibm.haploid.rest

import com.ibm.haploid.bootstrapper.HasTerminationHook
import com.ibm.haploid.core.concurrent.{ actorsystem ⇒ system }
import cc.spray.io.IoWorker

trait HaploidRestBase extends HasTerminationHook {

  def onTermination = 0

  implicit val actorSystem = system
  
  val log = logger

  val ioWorker = new IoWorker(system).start()

  system.registerOnTermination {
    ioWorker.stop()
  }

}