package com.ibm.haploid.rest
import com.ibm.haploid.core.concurrent._
import akka.util.Duration
import akka.util.duration._
import cc.spray.Directives
import cc.spray.Route
import com.ibm.haploid.rest.util.CompleteWithContextTrait

trait HaploidService extends Directives with CompleteWithContextTrait {

  implicit val actorSystem = actorsystem
  val service: Route

  lazy val shutdown = path("shutdown") { ctx ⇒
    ctx.complete("Shutting down now. This service will require a manual restart on its host to become available again, unless the service is configured to restart.")
    in(100.millis) {
      actorSystem.shutdown()
      System.exit(-1001)
    }
  }

  lazy val restart = path("restart") { ctx ⇒
    ctx.complete("Restarting this service now. It should be available again in less than a minute.")
    in(100.millis) {
      actorSystem.shutdown()
      System.exit(0)
    }
  }

  def in[U](duration: Duration)(body: ⇒ U) {
    actorSystem.scheduler.scheduleOnce(duration, new Runnable { def run() { body } })
  }

}