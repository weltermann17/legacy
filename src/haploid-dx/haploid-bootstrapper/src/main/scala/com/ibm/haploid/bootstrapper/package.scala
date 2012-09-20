package com.ibm.haploid

import scala.collection.JavaConversions.asScalaBuffer

/**
 * A bootstrapping framework.
 */
package object bootstrapper {

  import core.config._

  val mainclass = getString("haploid.bootstrapper.main-class")

  val restart = getBoolean("haploid.bootstrapper.restart")

  val pausebeforerestart = getMilliseconds("haploid.bootstrapper.pause-before-restart")

  val maximumrestarts = getInt("haploid.bootstrapper.maximum-restarts")

  val exitcodetostoprestarting = getInt("haploid.bootstrapper.exit-code-to-stop-restarting")

  val jvmoptions = getStringList("haploid.bootstrapper.jvm-options").toList

  require(0 < mainclass.length, "Did you forget to set haploid.bootstrapper.main-class in your configuration?")

  if (restart) require(1000 <= pausebeforerestart, "haploid.bootstrapper.pause-before-restart must be set to at least 1s in order to avoid too many spawned processes in case of an error.")

}

