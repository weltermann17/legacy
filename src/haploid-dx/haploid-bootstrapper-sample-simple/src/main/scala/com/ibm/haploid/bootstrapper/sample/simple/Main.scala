package com.ibm.haploid

package bootstrapper

package sample

package simple

import akka.actor.ActorSystem
import core.concurrent.schedule

object Main extends App {
  try {
    System.out.println(com.ibm.haploid.core.version)
    println("This is the very simple main class: " + getClass.getName)
    schedule(1000, 1000) { val a = ActorSystem.create; println(a); a.shutdown }
    Thread.sleep(3000)
    println("Main ended.")
  } finally {
    System.exit(0)
  }
}

