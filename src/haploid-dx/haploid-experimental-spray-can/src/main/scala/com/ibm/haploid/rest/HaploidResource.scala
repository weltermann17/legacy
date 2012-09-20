package com.ibm.haploid.rest
import cc.spray.RequestContext
import cc.spray.Directives
import akka.actor.ActorSystem
import cc.spray.directives.PathMatcher0
import com.ibm.haploid.core.concurrent._

trait HaploidResource extends Directives {

  implicit val actorSystem = actorsystem
  val pathElement: PathMatcher0

  def executeRequest(): RequestContext ⇒ Unit

}