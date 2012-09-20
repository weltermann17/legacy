package com.ibm.haploid

package dx

package engine

import collection.JavaConversions._
import akka.util.Timeout

/**
 *
 */
package object domain {

  import core.config._

  val infotimeout = Timeout(getMilliseconds("haploid.dx.engine.domain.info-timeout"))

  val infomaxduration = infotimeout.duration

  implicit val executorcontext = core.concurrent.actorsystem.dispatcher

  val pauseduringcreate = getMilliseconds("haploid.dx.engine.domain.pause-during-create")

  val retriesduringcreate = getInt("haploid.dx.engine.domain.retries-during-create")
  
} 

