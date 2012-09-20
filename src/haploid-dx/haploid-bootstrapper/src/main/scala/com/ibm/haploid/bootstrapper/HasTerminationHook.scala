package com.ibm.haploid

package bootstrapper

import java.io.{ LineNumberReader, InputStreamReader }

import core.concurrent.{ spawn, actorsystem }
import core.logger

trait HasTerminationHook {

  import HasTerminationHook._

  def onTermination: Int

  spawn {
    val reader = new LineNumberReader(new InputStreamReader(System.in))
    val msg = reader.readLine
    println(msg)
    if (terminationtoken == msg) {
      val exitcode = onTermination
      actorsystem.shutdown
      System.exit(exitcode)
    }
  }

}

object HasTerminationHook {

  val terminationtoken = "ctrl-c"

}

