package de.man.mn.gep.scala.config.enovia5.metadata.inmemory

import java.util.concurrent.Future

import com.ibm.de.ebs.plm.scala.concurrent.ops.future
import com.ibm.de.ebs.plm.scala.concurrent.ops.future2richfuture
import com.ibm.de.ebs.plm.scala.concurrent.ops.schedule
import com.ibm.de.ebs.plm.scala.concurrent.ops.spawn
import com.ibm.de.ebs.plm.scala.database.Raw
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.util.Timers.Int2Value
import com.ibm.de.ebs.plm.scala.util.Timers.time

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document.Documents
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document.Formats
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document.VersionDocuments
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.partner.PartnerMappings
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.partner.Partners
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.AssemblyRelations
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.Instances
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.HiddenInstances
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.Products
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.Versions

object Repository {

  def bootstrap(initial: Long, repeated: Long)(implicit connectionfactory: ConnectionFactory) = {
    val needreset = new java.util.concurrent.atomic.AtomicBoolean(false)
    schedule(initial, repeated) {
      try {
        if (null == next.get) {
          if (needreset.getAndSet(true)) InMemoryTable.reset
          val newtableset: Future[TableSet] = future {
            val ms = time {
              do {
                next.set(new TableSet)
                val needreset = error.getAndSet(false)
                val all = List(
                  fill(classOf[Permissions], needreset),
                  fill(classOf[Partners], needreset),
                  fill(classOf[PartnerMappings], needreset),
                  fill(classOf[Products], needreset),
                  fill(classOf[Versions], needreset),
                  fill(classOf[AssemblyRelations], needreset),
                  fill(classOf[Documents], needreset),
                  fill(classOf[Formats], needreset),
                  fill(classOf[HiddenInstances], needreset),
                  fill(classOf[VersionDocuments], needreset))
                all.foreach(_.get(5 minutes))
              } while (error.get)
            }
            println("bootstrapping in-memory : " + ms + " ms")
            val runtime = Runtime.getRuntime
            println("memory free/max/total : " + runtime.freeMemory + " " + runtime.maxMemory + " " + runtime.totalMemory)
            if (null != System.getProperty("repository.garbagecollection")) {
              runtime.runFinalization
              runtime.gc
              println("memory free/max/total : " + runtime.freeMemory + " " + runtime.maxMemory + " " + runtime.totalMemory)
            }
            current.get match {
              case null => afterFill; next.getAndSet(null)
              case _ => afterFill; current.set(future(next.getAndSet(null))); null
            }
          }
          current.get match {
            case null => current.set(newtableset)
            case _ =>
          }
        }
      } catch {
        case e =>
          e.printStackTrace
          println("Repository : " + e)
          println("Repository : Fatal error, program will abort now."); println
          Runtime.getRuntime.exit(-1)
      }
    }
  }

  private[inmemory] def setError = {
    if (null != System.getProperty("repository.ignoreerrors")) {
      println("Repository will ignore all errors.")
    } else {
      println("An error occurred. Need to load all tables from database.")
      error.set(true)
    }
  }

  private[inmemory] def hasError = error.get

  def apply[T <: InMemoryTable](c: Class[T]): T = current.get.get.get(name(c)).get.asInstanceOf[T]

  def next[T <: InMemoryTable](c: Class[T]): T = next.get.get(name(c)).get.asInstanceOf[T]

  private def fill[T <: InMemoryTable](c: Class[T], needreset: Boolean)(implicit connectionfactory: ConnectionFactory, f: Manifest[T#F], m: Manifest[T]): Future[T] = {
    next.get.get(name(c)) match {
      case null =>
        val t = new Filler[T] {}.fill(needreset)(connectionfactory, f, m)
        next.get.put(name(c), t.asInstanceOf[Future[InMemoryTable]])
        t
      case t => t.asInstanceOf[Future[T]]
    }
  }

  private def name[T <: InMemoryTable](c: Class[T]) = c.getSimpleName.toLowerCase

  private def afterFill(implicit connectionfactory: ConnectionFactory) = spawn {
    Thread.sleep(100)
    val instances = Instances(Raw("5EB7D58200F1D0424C3DB02A000130E2"))
    println("prefetched Instances")
  }

  private implicit val scheduler = de.man.mn.gep.scala.Server.getTaskService

  private type TableSet = java.util.concurrent.ConcurrentHashMap[String, Future[InMemoryTable]]

  private type NextTableSet = java.util.concurrent.atomic.AtomicReference[TableSet]

  private type CurrentTableSet = java.util.concurrent.atomic.AtomicReference[Future[TableSet]]

  private val current = new CurrentTableSet

  private val next = new NextTableSet

  private val error = new java.util.concurrent.atomic.AtomicBoolean(false)

}