package eu.man.phevos

package dx

package engine

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.duration.intToDurationInt
import akka.util.Timeout

import com.ibm.haploid.core.concurrent.{ actorsystem ⇒ system }
import com.ibm.haploid.core.inject.BaseBindingModule
import com.ibm.haploid.core.config
import com.ibm.haploid.core.newLogger
import com.ibm.haploid.core.util.text.stackTraceToString
import com.ibm.haploid.dx.engine.domain.EngineFSM
import com.ibm.haploid.dx.engine.event.Redo
import com.ibm.haploid.dx.engine.journal.journal
import com.ibm.haploid.rest.HaploidRestServer

object Engine

  extends App

  with HaploidRestServer {
  
  val ensurefirstload = config

  override def onTermination = {
    logger.info("This is the engine's shutdown.")
    0
  }

  try {

    object binding extends BaseBindingModule({ module ⇒
      import module._
      bind[ActorRef] identifiedBy 'journal toSingle journal
    })

    val engine = EngineFSM(binding)
    (journal ? Redo)(Timeout(15 minutes)) onSuccess {
      case s ⇒

        logger.info("Engine started successfully.")
        println("Engine started successfully.")

    } onFailure {

      case e: Throwable ⇒

        logger.error("Unable to start Engine " + e.getMessage())
        logger.error(stackTraceToString(e))

        println("Unable to start Engine")
        e.printStackTrace()

        system.shutdown

    }

    system.awaitTermination

  } catch {

    case e: Throwable ⇒ e.printStackTrace

  } finally {

    logger.info("System exit")
    System.exit(0)

  }

}
