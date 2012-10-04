package com.ibm.haploid

import java.io.{ PrintStream, FileOutputStream }
import java.nio.file.{ Paths, Files }

import scala.collection.JavaConversions.asScalaBuffer
import scala.concurrent.util.Duration

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging, EventStream, BusLogging }

import org.slf4j.LoggerFactory

import com.typesafe.config.{ ConfigFactory, Config }

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.util.StatusPrinter
import logging.LoggerNameFilter

object HaploidSystem {

  val version = "1.1.0-SNAPSHOT"

  val envHome = System.getenv("HAPLOID_HOME") match {
    case null | "" ⇒ None
    case v ⇒
      System.setProperty("haploid.home", v)
      Some(v)
  }

  val sysHome = System.getProperty("haploid.home") match {
    case null | "" ⇒ None
    case v ⇒ Some(v)
  }

  val home = sysHome orElse envHome

  def apply(classloader: ClassLoader): HaploidSystem = apply("haploid-library", classloader)

  def apply(name: String, classloader: ClassLoader): HaploidSystem = apply(name, ConfigFactory.load(classloader), classloader)

  def apply(name: String, config: Config, classloader: ClassLoader): HaploidSystem = {
    new HaploidSystemImpl(name, config, classloader).start
  }

  class Settings( final val name: String, cfg: Config, classloader: ClassLoader) {

    final val config: Config = {
      val config = cfg.withFallback(ConfigFactory.defaultReference(classloader))
      config.checkValid(ConfigFactory.defaultReference(classloader), "haploid")
      config
    }

    import config._

    final val version = getString("haploid.version")

    final val home = getString("haploid.home") match {
      case null | "" ⇒ None
      case v ⇒ Some(v)
    }

    final val temp = try {
      val tmp = getString("haploid.temp")
      Files.createDirectories(Paths.get(tmp))
      System.setProperty("java.io.tmpdir", tmp)
      tmp
    } catch {
      case e: Throwable ⇒
        println(e)
        System.getProperty("java.io.tmpdir")
    }

    final val logConfigOnStart = getBoolean("haploid.log-config-on-start")

    if (version != HaploidSystem.version) throw new IllegalArgumentException(String.format("haploid.version (%s) does not match internal version (%s).", version, HaploidSystem.version))

    override def toString = config.root.render

    final val logginglevel = {
      val level = getString("haploid.logging.level").toUpperCase
      System.setProperty("rootLevel", level match {
        case "WARNING" ⇒ "WARN"
        case v ⇒ v
      })
      level
    }

    final val loggingConsole = new LogSettings("haploid.logging.console")

    final val loggingPlainText = new LogSettings("haploid.logging.plain-text")

    final val loggingHtml = new LogSettings("haploid.logging.html")

    final val filterDebugLoggerNames: List[String] = try {
      getStringList("haploid.logging.filter-debug-logger-names").toList
    } catch {
      case _: Throwable ⇒ List.empty
    }

    class LogSettings(path: String) {

      private[this] val cfg = config.getConfig(path)

      final val enable = cfg.getBoolean("enable")

      System.setProperty(path + ".enable", enable.toString)

      final val toFile = if (enable) cfg.getString("file") match {
        case "" | "." | null ⇒ false
        case v ⇒
          System.setProperty(path + ".file", v)
          true
      }
      else false

      if (enable) cfg.getString("pattern") match {
        case "" | null ⇒
        case v ⇒ System.setProperty(path + ".pattern", v)
      }

      if (enable) cfg.getString("rolling-pattern") match {
        case "" | null ⇒
        case v ⇒ System.setProperty(path + ".rolling-pattern", v)
      }

    }

  }

}

abstract class HaploidSystem {

  import HaploidSystem._

  def name: String

  def settings: Settings

  def log: LoggingAdapter

  def newLogger(any: Any): LoggingAdapter

  def actorSystem: ActorSystem

  def shutdown: Unit

  def isTerminated: Boolean

  def awaitTermination(timeout: Duration)

  def awaitTermination

}

private[haploid] class HaploidSystemImpl(val name: String, config: Config, classloader: ClassLoader)

  extends HaploidSystem {

  import HaploidSystem._

  final val settings = new Settings(name, config, classloader)

  import settings._

  final val logSystem = {
    val system = ActorSystem(name + "-logging")
    system.eventStream.setLogLevel(Logging.levelFor(logginglevel).getOrElse(Logging.DebugLevel))
    system
  }

  final val log = _log

  final val actorSystem = ActorSystem(name, config, classloader)

  def newLogger(any: Any) = Logging.getLogger(actorSystem.eventStream, any.getClass)

  def shutdown = {
    actorSystem.shutdown
    logSystem.eventStream.setLogLevel(Logging.InfoLevel)
    logSystem.shutdown
  }

  def isTerminated = {
    actorSystem.isTerminated && logSystem.isTerminated
  }

  def awaitTermination(timeout: Duration) = {
    actorSystem.awaitTermination(timeout)
    logSystem.awaitTermination(timeout)
  }

  def awaitTermination = awaitTermination(Duration.Inf)

  def start = _start

  private[this] lazy val _start: this.type = {
    if (logConfigOnStart) log.info(settings.toString)
    this
  }

  private[this] lazy val _log: LoggingAdapter = {
    LoggerNameFilter.filterDebugLoggerNames = filterDebugLoggerNames
    val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    try {
      val configurator = new JoranConfigurator
      configurator.setContext(context)
      context.reset
      configurator.doConfigure(getClass.getClassLoader.getResourceAsStream("logback-config.xml"))
      if (loggingConsole.enable && loggingConsole.toFile) {
        try {
          val path = Paths.get(System.getProperty("haploid.logging.console.file"))
          Files.createDirectories(path.getParent)
          val console = new PrintStream(new FileOutputStream(path.toFile))
          System.setOut(console)
          System.setErr(console)
        } catch {
          case e: Throwable ⇒
            println("Could not create log-console-file : " + e)
        }
      }
    } catch {
      case e: Throwable ⇒
        e.printStackTrace
    }
    StatusPrinter.printInCaseOfErrorsOrWarnings(context)
    Logging.getLogger(logSystem, name)
  }

}
  
  