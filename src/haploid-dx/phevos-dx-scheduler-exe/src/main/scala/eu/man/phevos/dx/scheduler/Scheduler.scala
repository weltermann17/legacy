package eu.man.phevos

package dx

package scheduler

import com.ibm.haploid.rest.HaploidRestServer

object Scheduler extends App with HaploidRestServer {

  override def onTermination = {
    println("This is the scheduler shutdown.")
    0
  }

  println("This is the scheduler startup.")
  DiningHakkers.run

}

