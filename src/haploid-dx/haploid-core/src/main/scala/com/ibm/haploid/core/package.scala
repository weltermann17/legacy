package com.ibm.haploid

import java.io.{ File, PrintWriter, StringWriter, PrintStream, FileOutputStream }
import java.net.URL

import collection.JavaConversions._

import scala.io.Source.fromFile
import scala.io.Source.fromURL

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import org.apache.commons.lang3.SystemUtils.{ IS_OS_UNIX, IS_OS_WINDOWS }

import org.slf4j.LoggerFactory

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter

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
          case (r, "", "") ⇒ fromURL(this.getClass.getResource(r))
          case ("", u, "") ⇒ fromURL(new URL(u))
          case ("", "", f) ⇒ fromFile(f)
          case _ ⇒ throw new RuntimeException("You need to define one of haploid.config-scala.(resource | file | url) with haploid.config-scala.enable set to true.")
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

  val bootstrapping = try { getBoolean("haploid.bootstrapping") } catch { case _: Throwable ⇒ false }

  val version = getString("haploid.core.version")

  val loggername = getString("haploid.core.logger-name")

  val loglevel = getString("haploid.core.log-level")

  System.setProperty("haploid.core.log-level", loglevel)

  val logconsole = getBoolean("haploid.core.log-console")

  val logfile = getString("haploid.core.log-file")

  System.setProperty("haploid.core.log-file", logfile)

  val logfilehtml = getString("haploid.core.log-file-html")

  System.setProperty("haploid.core.log-file-html", logfilehtml)

  val logfilerollingpattern = getString("haploid.core.log-file-rolling-pattern")

  System.setProperty("haploid.core.log-file-rolling-pattern-logback", logfilerollingpattern)

  val logfilehtmlrollingpattern = getString("haploid.core.log-file-html-rolling-pattern")

  System.setProperty("haploid.core.log-file-html-rolling-pattern-logback", logfilehtmlrollingpattern)

  val logpattern = getString("haploid.core.log-pattern")

  System.setProperty("haploid.core.log-pattern", logpattern)

  lazy val loggingconfig = if (!bootstrapping) {
    val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    try {
      val configurator = new JoranConfigurator
      configurator.setContext(context)
      context.reset
      configurator.doConfigure(getClass.getClassLoader.getResourceAsStream(
        if (logconsole) "logback-console.xml" else "logback-file.xml"))
      configurator.getStatusManager.getCopyOfStatusList.foreach(println)
    } catch {
      case e: Throwable ⇒
        val configurator = new JoranConfigurator
        configurator.setContext(context)
        configurator.getStatusManager.getCopyOfStatusList.foreach(println)
        e.printStackTrace
    }
    StatusPrinter.printInCaseOfErrorsOrWarnings(context)
  }

  val logger = {
    val l = loggingconfig
    Logging.getLogger(concurrent.actorsystem.eventStream, loggername)
  }

  val logconfigonstart = getBoolean("haploid.core.log-config-on-start")

  if (logconfigonstart) logger.error("\n" + util.json.Json.prettyPrint(root.unwrapped))

  val logconsoletofile = getBoolean("haploid.core.log-console-to-file")

  val logconsolefile = getString("haploid.core.log-console-file")

  if (logconsoletofile) try {
    val consolefile = new PrintStream(new FileOutputStream(logconsolefile))
    System.setOut(consolefile)
    System.setErr(consolefile)
  } catch {
    case e: Throwable ⇒
      logger.error("Could not create log-console-file : " + e)
  }

  def newLogger(any: Any) = Logging.getLogger(concurrent.actorsystem.eventStream, any.getClass)

  def newLogger(name: String) = Logging.getLogger(concurrent.actorsystem.eventStream, name)

  val operatingsystem = if (IS_OS_UNIX) "unix" else if (IS_OS_WINDOWS) "windows" else "unknown"

  val machinename = if (IS_OS_UNIX) "must_call_hostname" else if (IS_OS_WINDOWS) System.getenv("COMPUTERNAME").toLowerCase else "unknown"

  /**
   * Call this from anywhere in order to terminate the jvm with a message and a given exit code.
   */
  def terminateJvm(reason: Throwable, code: Int): Nothing = try {
    try { concurrent.actorsystem.shutdown; Thread.sleep(1000) } catch { case _: Throwable ⇒ }
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
    case e: Throwable ⇒
      Runtime.getRuntime.exit(code)
      throw reason
  }

}

