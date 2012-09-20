package de.man.mn.gep.scala.config.enovia5.metadata.inmemory

import java.io.OutputStream
import java.io.PrintWriter
import java.io.File
import java.io.FileFilter
import java.nio.channels.Channels
import java.nio.channels.WritableByteChannel
import java.util.concurrent.Future

import scala.collection.JavaConversions._

import org.restlet.data.MediaType
import org.restlet.resource.Get
import org.restlet.resource.ServerResource

import com.ibm.de.ebs.plm.scala.concurrent.ops.future
import com.ibm.de.ebs.plm.scala.concurrent.ops.spawn
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichPreparedStatement
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.ps2Rich
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper
import com.ibm.de.ebs.plm.scala.database.Raw
import com.ibm.de.ebs.plm.scala.json.Json._
import com.ibm.de.ebs.plm.scala.resource.b
import com.ibm.de.ebs.plm.scala.resource.disposable
import com.ibm.de.ebs.plm.scala.resource.forceContextType
import com.ibm.de.ebs.plm.scala.resource.using
import com.ibm.de.ebs.plm.scala.rest.WritableByteChannelRepresentation
import com.ibm.de.ebs.plm.scala.util.Io.nullstream
import com.ibm.de.ebs.plm.scala.util.Timers.time
import com.ibm.de.ebs.plm.scala.util.Timers.now

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import oracle.jdbc.OracleConnection
import com.ibm.de.ebs.plm.scala.util.Timers.time

abstract class TableFiller[T <: InMemoryTable] {

  def fill(implicit cons: java.lang.reflect.Constructor[_]): T

  protected def newTable(connectionfactory: ConnectionFactory, length: Int)(implicit cons: java.lang.reflect.Constructor[_]): T = {
    cons.newInstance(connectionfactory, new java.lang.Integer(length)).asInstanceOf[T]
  }

}

abstract class InMemoryTableFiller[T <: InMemoryTable](implicit connectionfactory: ConnectionFactory)
  extends TableFiller[T] {

  private def doFill(implicit cons: java.lang.reflect.Constructor[_]): T = {
    var inmemorytable: InMemoryTable = null
    var i = 0
    try {
      Thread.sleep(200)
      if (!Repository.hasError) {
        for (row <- prepare <<! (result => result)) {
          val total: Int = row
          if (0 == i) inmemorytable = newTable(connectionfactory, total)
          inmemorytable(i, row)
          i += 1
          if (0 == i % 10000 && Repository.hasError) throw new InterruptedException
          if (0 == i % 100000) println("load " + getClass.getSimpleName + " " + i)
        }
      }
      if (0 == i) {
        inmemorytable = newTable(connectionfactory, 0)
      }
      if (1 < i) println("rows loaded " + getClass.getSimpleName + " " + i)
    } catch {
      case e: InterruptedException => println("load interrupted " + getClass.getSimpleName)
      case e => throw e
    }
    inmemorytable.afterFill.asInstanceOf[T]
  }

  def fill(implicit cons: java.lang.reflect.Constructor[_]): T = {
    implicit val scheduler = de.man.mn.gep.scala.Server.getTaskService
    try {
      using {
        implicit val _ = forceContextType[T]
        val connection = disposable(connectionfactory.newConnection())
        val oracleconnection = connection.unwrap(classOf[oracle.jdbc.OracleConnection])
        oracleconnection.setDefaultRowPrefetch(8 * 1024)
        val stmt = disposable(connection.prepareStatement(sql))
        statement = stmt
        doFill
      }
    } catch {
      case e =>
        e.printStackTrace
        println("fill, " + getClass.getSimpleName + " --> " + sql + " " + e)
        throw e
    }
  }

  protected val sql: String

  protected def prepare: RichPreparedStatement = statement

  protected implicit var statement: RichPreparedStatement = null

}

trait InMemoryTable extends Serializable {

  type K

  type F

  val shortname = getClass.getSimpleName.toLowerCase

  def unique(value: K): Int = id.unique(value)

  def set(index: Int, row: RichResultSet)

  def row(index: Int): Map[String, _] = columns.mapValues { col =>
    try {
      col.get(index) match {
        case None | null => null
        case Some(v) => v
        case v => v
      }
    } catch {
      case _ => null
    }
  }.filter(null != _._2)

  lazy val size = if (0 < columns.size) columns.values.toList(0).length else -1

  def print(index: Int, writer: PrintWriter) = writer.print(build(row(index)))

  def dump(n: Int = size) = (0 until math.min(n, size)).foreach(i => println(build(row(i))))

  def apply(index: Int, row: RichResultSet) = set(index, row)

  def apply(value: K): Int = unique(value)

  def select[T](subset: Set[Int], column: Column[T]): Seq[T] = {
    subset.foldLeft(List[T]()) { case (s, i) => column.get(i) :: s }
  }

  def distinct[T](subset: Set[Int], column: Column[T]): Set[T] = {
    subset.foldLeft(Set[T]()) { case (s, i) => s + column.get(i) }
  }

  def groupByRowId[T](subset: Set[Int], column: Column[T]): Set[(Int, Int)] = {
    subset.foldLeft(Map[T, (Int, Int)]()) {
      case (m, i) =>
        val k = column.get(i)
        val v = m.getOrElse(k, (i, 0))
        m ++ Map(k -> (v._1, 1 + v._2))
    }.values.toSet
  }

  def groupBy[T](subset: Set[Int], column: Column[T], rowid: Boolean): Set[(T, Int)] = {
    val map = new collection.mutable.HashMap[T, Int]
    subset.foreach { i => map.put(column.get(i), 1 + map.getOrElse(column.get(i), 0)) }
    map.toSet
  }

  val columns: Map[String, Column[_]]

  val id: Unique[K]

  private[inmemory] def setComplete: Unit = InMemoryTable.setComplete(this)

  private[inmemory] def afterFill: InMemoryTable = { columns.values.foreach(_.afterFill); this }

  protected val timestamp: Long = InMemoryTable.setOrGetTimestamp(now)

}

trait Filler[T <: InMemoryTable] {

  type F = T#F

  def fill(needreset: Boolean)(implicit connectionfactory: ConnectionFactory, f: Manifest[F], t: Manifest[T]): Future[T] = {
    table = doFill(needreset)
    table
  }

  def get: T = table.get

  private def doFill(needreset: Boolean)(implicit connectionfactory: ConnectionFactory, f: Manifest[F], m: Manifest[T]): Future[T] = {
    implicit val cons = m.erasure.getConstructors()(0)
    implicit val c = cons.getName
    implicit val scheduler = de.man.mn.gep.scala.Server.getTaskService
    future {
      try {
        InMemoryTable.deserialize(needreset) match {
          case None =>
            val filler = try {
              f.erasure.getConstructors()(0).newInstance(connectionfactory).asInstanceOf[F]
            } catch {
              case _ => f.erasure.getConstructors()(0).newInstance().asInstanceOf[F]
            }
            val t = filler.asInstanceOf[TableFiller[T]].fill.asInstanceOf[T]
            t.setComplete
            spawn { InMemoryTable.serialize(t) }
            t
          case Some(t) => t
        }
      } catch { case e => Repository.setError; println(e); throw e }
    }
  }

  private var table: Future[T] = null

}

object InMemoryTable {

  private[inmemory] def serialize(t: InMemoryTable)(implicit c: String) = {
    if (Repository.hasError) throw new InterruptedException("serialize")
    try {
      using {
        implicit val _ = forceContextType[Unit]
        val file = filepath(c)
        val out = disposable(new java.io.ObjectOutputStream(new java.io.BufferedOutputStream(new java.util.zip.GZIPOutputStream(new java.io.FileOutputStream(file)), buffersize)))
        out.writeObject(t)
        println("serialized " + t.getClass.getSimpleName + " (" + file + ")")
      }
    } catch { case e => remove(c); println(e) }
  }

  private[inmemory] def deserialize[T <: InMemoryTable](needreset: Boolean)(implicit c: String): Option[T] = {
    def ignore(e: Exception): Option[T] = { println("Need to load from database : " + e); None }
    try {
      if (needreset) remove(c)
      using {
        implicit val _ = forceContextType[Option[T]]
        val file = filepath(c)
        if (!exists(c)) throw new java.io.FileNotFoundException(file)
        val in = disposable(new java.io.ObjectInputStream(new java.io.BufferedInputStream(new java.util.zip.GZIPInputStream(new java.io.FileInputStream(file)), buffersize)))
        val t = in.readObject.asInstanceOf[T]
        t.setComplete
        println("deserialized " + t.getClass.getSimpleName + " (" + file + ")")
        Some(t)
      }
    } catch {
      case e: java.io.EOFException => Repository.setError; remove(c); ignore(e)
      case e: java.io.FileNotFoundException => if (!needreset) Repository.setError; ignore(e)
      case e: java.io.InvalidClassException => Repository.setError; remove(c); ignore(e)
      case e: TimestampOutOfSyncException => Repository.setError; remove(c); ignore(e)
      case e => Repository.setError; remove(c); e.printStackTrace; None
    }
  }

  private def filepath(c: String) = directory + com.ibm.de.ebs.plm.scala.security.MessageDigest.MD5(c) + extension

  private def exists(c: String) = new java.io.File(filepath(c)).exists

  private def remove(c: String) = if (exists(c)) new java.io.File(filepath(c)).delete

  private lazy val directory = {
    if (new File("c:/temp").exists) "c:/temp/"
    else if (new File("/tmp/GEP/ramdisk").exists) "/tmp/GEP/ramdisk/"
    else if (new File("/tmp/ramdisk").exists) "/tmp/ramdisk/"
    else if (new File("/tmp").exists) "/tmp/"
    else throw new java.io.IOException("PersistentKeyMap : directory not found")
  }

  private[inmemory] def reset = {
    println("Reset all tables.")
    clearSnapshots
  }

  private def setOrGetTimestamp(t: Long): Long = timestamp.compareAndSet(0, t) match {
    case true => t
    case false => timestamp.get
  }

  private def setComplete(inmemorytable: InMemoryTable) = {
    if (inmemorytable.timestamp != setOrGetTimestamp(inmemorytable.timestamp)) throw new TimestampOutOfSyncException
  }

  private def clearSnapshots = {
    val dir = new File(directory)
    if (dir.isDirectory) {
      val files = dir.listFiles(new FileFilter { def accept(f: java.io.File) = f.getPath.endsWith(extension) })
      files.foreach(_.delete)
    }
  }

  private val timestamp = new java.util.concurrent.atomic.AtomicLong(0)

  private val buffersize = 32 * 1024 * 1024

  private class TimestampOutOfSyncException extends Exception

  private val extension = ".snapshot.gep"

}

abstract class LinkTable(implicit length: Int) extends HasPermissions {

  type K = Int

  type Link <: InMemoryTable
  type From <: HasPermissions
  type To <: InMemoryTable

  val id = new UniqueColumn[Int]
  val from = new CompressedColumn[Int]
  val to = new CompressedColumn[Int]
  val project = new BitSetColumn[String]

  val columns = Map(
    "id" -> id,
    "from" -> from,
    "to" -> to,
    "project" -> project)

  def set(index: Int, row: RichResultSet) = ()

}

