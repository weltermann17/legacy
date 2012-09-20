package com.ibm.haploid

import java.io.{ File, PrintWriter, StringWriter }
import java.net.URL

import scala.io.Source.fromFile
import scala.io.Source.fromURL

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import org.apache.commons.lang3.SystemUtils.{IS_OS_UNIX, IS_OS_WINDOWS}

import akka.event.Logging

/**
 * Core components building the foundation of the haploid framework.
 */
package object core {

  val config = {
    val config = {
      val config = ConfigFactory.load
      import config._
      object configscala {
        val enable = getBoolean("haploid.config-scala.enable")
        val resource = getString("haploid.config-scala.resource")
        val uri = getString("haploid.config-scala.url")
        val file = getString("haploid.config-scala.file")
        val target = getString("haploid.config-scala.target-directory")
      }
      import configscala._
      if (enable) {
        val directory = new File(target)
        val source = ((resource, uri, file) match {
          case (r, "", "") => fromURL(this.getClass.getResource(r))
          case ("", u, "") => fromURL(new URL(u))
          case ("", "", f) => fromFile(f)
          case _ => throw new RuntimeException("You need to define one of haploid.config-scala.(resource | file | url) with haploid.config-scala.enable set to true.")
        }).mkString
        val compiler = new core.compiler.Compiler(source, directory)
        ConfigFactory.load(compiler.apply[Config].withFallback(config))
      } else {
        config
      }
    }
    ConfigFactory.load(config.getConfig("haploid").withOnlyPath("akka").withFallback(config))
  }

  import config._

  val version = getString("haploid.core.version")

  val loggername = getString("haploid.core.logger-name")

  val logger = Logging(concurrent.actorsystem, loggername)

  val logconfigonstart = getBoolean("haploid.core.log-config-on-start")

  if (logconfigonstart) logger.info(util.json.Json.prettyPrint(root.unwrapped))
  
  val operatingsystem = if (IS_OS_UNIX) "unix" else if (IS_OS_WINDOWS) "windows" else "unknown"

  /**
   * Call this from anywhere in order to terminate the jvm with a message and a given exit code.
   */
  def terminateJvm(reason: Throwable, code: Int): Nothing = try {
    try { concurrent.actorsystem.shutdown; Thread.sleep(1000) } catch { case _ => }
    logger.error(util.text.stackTraceToString(reason))
    val message = """
haploid-core : %s
haploid-core : Memory free/max/total : %d %d %d
haploid-core : Program will abort now."""
    val runtime = Runtime.getRuntime
    logger.error(message.format(reason, runtime.freeMemory, runtime.maxMemory, runtime.totalMemory))
    println(message.format(reason, runtime.freeMemory, runtime.maxMemory, runtime.totalMemory)); println(".")
    runtime.exit(code)
    throw reason
  } catch {
    case _ =>
      Runtime.getRuntime.exit(code)
      throw reason
  }

} 

