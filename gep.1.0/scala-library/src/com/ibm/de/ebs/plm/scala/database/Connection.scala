package com.ibm.de.ebs.plm.scala.database

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic

import com.ibm.de.ebs.plm.scala.concurrent.ops.OnlyOnce
import com.ibm.de.ebs.plm.scala.concurrent.ops.schedule
import com.ibm.de.ebs.plm.scala.concurrent.ops.spawn
import com.ibm.de.ebs.plm.scala.util.Timers._
import com.ibm.de.ebs.plm.scala.util.Timers.now

class Connection(connection: java.sql.Connection) extends ConnectionWrapper(connection) {

  override def close = {
    if (!connection.isClosed) {
      connection.clearWarnings()
      if (!ConnectionFactory.idle.contains(this)) {
        deactivate
        ConnectionFactory.idle.putFirst(this)
      } else {
        throw new Exception("Connection already in idle list : " + this)
      }
    } else {
      doClose
    }
  }

  override def toString = super.toString + " " + isActive

  def isActive = active.get

  def activate = {
    active.set(true)
  }

  def deactivate = {
    active.set(false)
  }

  private[database] def doClose = super.close()
  private[database] val lastaccessed = new atomic.AtomicLong(now)
  private var active = new atomic.AtomicBoolean(false)
}

trait ConnectionFactory extends OnlyOnce {

  def init: ConnectionFactory

  def newConnection(timeout: Long = pooltimeout)(implicit scheduler: ScheduledExecutorService) = {
    getConnection(timeout) match {
      case Some(connection) => connection
      case None =>
        throw new java.sql.SQLTimeoutException(
          "No connection available in pool, timeout " + timeout + "ms, poolmin " + poolmin + ", poolmax " + poolmax)
    }
  }

  def getConnection(timeout: Long = pooltimeout)(implicit scheduler: ScheduledExecutorService): Option[Connection] = {
    onlyonce { initialize }
    var connection: Option[Connection] = None
    var elapsed: Long = 0
    val interval: Long = growtimeout
    while (None == connection && elapsed < timeout) {
      connection = ConnectionFactory.idle.poll(interval, TimeUnit.MILLISECONDS) match {
        case null if ConnectionFactory.connections.size < poolmax =>
          val oracleconnection = datasource.getConnection.asInstanceOf[oracle.jdbc.OracleConnection]
          oracleconnection.setExplicitCachingEnabled(false)
          oracleconnection.setImplicitCachingEnabled(false)
          oracleconnection.setStatementCacheSize(0)
          oracleconnection.setDefaultRowPrefetch(1024)
          oracleconnection.setAutoCommit(false)
          val connection = new Connection(oracleconnection)
          ConnectionFactory.connections.add(connection)
          connection.activate
          Some(connection)
        case null =>
          elapsed += interval
          None
        case connection =>
          connection.lastaccessed.set(now)
          connection.activate
          Some(connection)
      }
    }
    if (elapsed > ConnectionFactory.peakelapsed.get) {
      ConnectionFactory.peakelapsed.set(elapsed)
      println("getConnection peak elapsed : " + elapsed)
    }
    if (None == connection) {
      closeConnections
      println("Critical error: no more connections available in pool. Aborting program.")
      Runtime.getRuntime.exit(1)
    }
    connection
  }

  override protected def finalize = {
    closeConnections
  }

  protected def initialize(implicit scheduler: ScheduledExecutorService) = {
    datasource.setURL(url)
    datasource.setImplicitCachingEnabled(true)
    spawn {
      val poolmins = new collection.mutable.ListBuffer[Connection]
      (0 until poolmin).foreach { i =>
        getConnection() match {
          case Some(connection) => poolmins += connection
          case None =>
        }
      }
      poolmins.foreach { _.close }
    }
    schedule(idletimeout) {
      if (ConnectionFactory.idle.size > poolmin) {
        ConnectionFactory.idle.peekLast match {
          case null =>
          case peekonly =>
            if (idletimeout < (now - peekonly.lastaccessed.get)) {
              ConnectionFactory.idle.pollLast match {
                case null =>
                case reallypoll if reallypoll == peekonly =>
                  ConnectionFactory.connections.remove(reallypoll)
                  reallypoll.doClose
              }
            }
        }
      }
    }
  }

  private def closeConnections = {
    try {
      import scala.collection.JavaConversions._
      ConnectionFactory.connections.keySet.foreach { connection =>
        connection.doClose
      }
      ConnectionFactory.connections.clear
      ConnectionFactory.idle.clear
    } catch {
      case e => println(e)
    }
  }

  var datasource = {
    val properties = new java.util.Properties
    properties.setProperty("oracle.jdbc.defaultNChar", "false")
    properties.setProperty("oracle.jdbc.useNio", "true")
    properties.setProperty("oracle.jdbc.useThreadLocalBufferCache", "true")
    properties.setProperty("oracle.jdbc.maxCachedBufferSize", 18.toString)
    val ds = new oracle.jdbc.pool.OracleDataSource
    ds.setConnectionProperties(properties)
    ds
  }

  val driver: String

  val url: String

  val poolmin: Int = 4

  val poolmax: Int = 8

  val idletimeout = 2 minutes

  val growtimeout = 5 milliseconds

  val pooltimeout = 2 minutes

}

private[database] object ConnectionFactory {

  private[database] val idle = new LinkedBlockingDeque[Connection]

  private val connections = new ConcurrentHashMap[Connection, Unit] {

    def add(connection: Connection) = put(connection, ())

    override def put(connection: Connection, o: Unit): Unit = {
      super.put(connection, o)
      if (size > peak.get) peak.set(size)
      println("Connections.add " + size + ", peak " + peak.get)
    }

    def remove(connection: Connection): Unit = {
      super.remove(connection)
      println("Connections.remove: connections " + size + ", peak " + peak.get + ", timeout " + (now - connection.asInstanceOf[Connection].lastaccessed.get) + " ms, idle " + idle.size)
    }

    private val peak = new atomic.AtomicInteger
  }

  private val peakelapsed = new atomic.AtomicLong

}
