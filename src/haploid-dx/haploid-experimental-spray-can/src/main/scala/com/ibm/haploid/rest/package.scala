package com.ibm.haploid

import com.typesafe.config.{ ConfigFactory, ConfigParseOptions, ConfigResolveOptions }
import akka.actor.ActorSystem

/**
 * A bootstrapping framework.
 */
package object rest {

  import core.config._
  import scala.collection.JavaConversions._

  val httpServiceActorName = getString("haploid.rest.http-service-actor")

  val rootServiceActorName = getString("haploid.rest.root-service-actor")

  val clientActorName = getString("haploid.rest.client.client-actor")

  val serverActorName = getString("haploid.rest.server-actor")

  val serverHost = getString("haploid.rest.server-host")

  val serverPort = getInt("haploid.rest.server-port")

  val restResources = getStringList("haploid.rest.dynamic-resources").toList

  val restMixins = getStringList("haploid.rest.dynamic-mixins").toList

  val serviceName = getString("haploid.rest.server-service")

  val service = Class.forName(serviceName).newInstance().asInstanceOf[HaploidService]

  val sslTruststore = getString("haploid.rest.ssl-truststore")

  val sslTruststorePassword = getString("haploid.rest.ssl-truststore-password")

}
