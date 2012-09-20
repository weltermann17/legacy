package com.ibm.haploid

package bootstrapper

package sample

package simple

import java.io.{ LineNumberReader, InputStreamReader }

import core.concurrent.{ spawn, schedule, actorsystem }

object Main extends App with HasTerminationHook {

  def onTermination = -1001

  try {
    System.out.println(com.ibm.haploid.core.version)
    println("This is the very simple main class: " + getClass.getName)
    schedule(1000, 1000) { val a = actorsystem; println(a); a.shutdown }
    Thread.sleep(3000)
    println("compute")
    var d = 0.0
    var i = 0L
    while (i < 10000000000L) { d += i.toDouble * 1.111; i += 1 }
    println("Main ended " + d)
  } catch {
    case e => e.printStackTrace
  } finally {
    println("System exit")
    System.exit(0)
  }

}

