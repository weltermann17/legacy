package com.ibm.haploid.rest

import com.typesafe.config.{ ConfigFactory, ConfigParseOptions, ConfigResolveOptions }
import akka.actor.ActorSystem

/**
 * A bootstrapping framework.
 */
package object client {

  import com.ibm.haploid.core.config._

  val clientActorName = getString("haploid.rest.client.client-actor")

}