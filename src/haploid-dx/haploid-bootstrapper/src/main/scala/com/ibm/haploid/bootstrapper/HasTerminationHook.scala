package com.ibm.haploid

package bootstrapper

import java.io.{ LineNumberReader, InputStreamReader }

import core.newLogger
import core.concurrent.{ spawn, actorsystem ⇒ system }

trait HasTerminationHook {

  import HasTerminationHook._

  def onTermination: Int

  val logger = newLogger(this)

  spawn {
    val reader = new LineNumberReader(new InputStreamReader(System.in))
    val msg = reader.readLine
    println(msg)
    logger.error(msg)
    if (terminationtoken == msg) {
      val exitcode = onTermination
      system.shutdown
      System.exit(exitcode)
    }
  }

}

object HasTerminationHook {

  val terminationtoken = "ctrl-c"

}

