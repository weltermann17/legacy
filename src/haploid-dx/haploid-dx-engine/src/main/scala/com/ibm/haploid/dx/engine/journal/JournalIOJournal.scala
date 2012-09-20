package com.ibm.haploid

package dx

package engine

package journal

import java.io.{ FileOutputStream, File }
import java.util.concurrent.atomic.AtomicLong

import javax.xml.bind.annotation.{ XmlType, XmlRootElement }

import scala.collection.JavaConversions._

import akka.actor.actorRef2Scala
import akka.actor.ActorPath
import akka.dispatch.{ MessageDispatcher, Await }
import akka.event.LoggingAdapter
import akka.pattern.ask

import core.concurrent.{ spawn, schedule }
import core.util.text.stackTraceToString

import event.Serialization.{ serialize, deserialize }
import event.{ ReceiverEvent, Exists }
import _root_.journal.io.api.{ Location, Journal ⇒ JournalIO, ReplicationTarget }

/**
 *
 */
class JournalIOJournal

  extends Journal {

  override protected def finalize = {
    syncInterval.cancel
    journal.sync
    journal.close
  }

  protected[this] def redo = redo(1, 1)

  protected[this] def doAppend(receiverevent: ReceiverEvent) = try {
    writecounter.incrementAndGet
    if (0 == writecounter.get % logmodulo) log.info("append " + writecounter)
    val location = journal.write(serialize(receiverevent), JournalIO.WriteType.ASYNC)
    if (0 == writecounter.get % 10) journal.sync
    val entry = JournalEntry(
      location.getDataFileId,
      location.getPointer,
      writecounter.get,
      receiverevent)
    log.debug("append " + entry)
    publish(entry, retriesduringredo)
    doBackup(receiverevent)
  } catch {
    case e: OutOfMemoryError ⇒ core.terminateJvm(e, -1)
    case e ⇒
      log.error("append failed  " + writecounter + " : " + e)
      writecounter.decrementAndGet
  }

  private[this] def iterator(fd: Long, entry: Long): Iterator[Option[JournalEntry]] = try {
    if (journal.getFiles.isEmpty)
      nulliterator
    else {
      val readcounter = new AtomicLong(-1L)
      journal.redo(new Location(fd.toInt, entry.toInt)).iterator.map { location ⇒
        try {
          readcounter.incrementAndGet
          if (0 == readcounter.get % logmodulo) log.info("redo " + readcounter + " / " + redocounter)
          val receiverevent = deserialize[ReceiverEvent](journal.read(location, JournalIO.ReadType.ASYNC))
          receiverevent.event.redo
          Some(JournalEntry(
            location.getDataFileId,
            location.getPointer,
            readcounter.get,
            receiverevent))
        } catch {
          case e ⇒
            log.error("redo skipped entry " + readcounter + " / " + redocounter + " at location " + location + " : " + stackTraceToString(e))
            addInvalidLocation(location)
            None
        }
      }
    }
  } catch {
    case e ⇒
      log.error("iterator failed : " + stackTraceToString(e)); nulliterator
  }

  private[this] object nulliterator extends Iterator[Option[JournalEntry]] {
    def hasNext = false
    def next = throw new NoSuchElementException
  }

  private[this] def redo(filedescriptor: Long, entry: Long) = try {
    iterator(filedescriptor, entry).foreach { entry ⇒
      entry match {
        case Some(entry) ⇒
          log.debug("redo " + entry)
          publish(entry, retriesduringredo)
        case None ⇒
      }
    }
    log.info("redo completed " + redocounter)
    removeInvalidLocations
  } catch {
    case e ⇒
      log.error("redo failed : " + stackTraceToString(e))
  }

  private[this] def addInvalidLocation(location: Location) = {
    if (removeentryifretriesfail) {
      log.warning("entry will be removed when redo is complete : " + location)
      invalidlocations += location
    }
  }

  private[this] def removeInvalidLocations = try {
    if (0 < invalidlocations.size) {
      val marker = journal.write("marker".getBytes, JournalIO.WriteType.SYNC)
      invalidlocations.foreach { location ⇒
        try {
          journal.delete(location)
          log.warning("removed location " + location)
        } catch {
          case e ⇒
            log.error("remove location failed " + location + " : " + stackTraceToString(e))
        }
      }
      try { journal.sync; journal.delete(marker); journal.sync } catch { case _ ⇒ }
      invalidlocations.clear
    }
  } catch {
    case e ⇒
      log.error("remove invalid locations failed : " + stackTraceToString(e))
  }

  private[this] def publish(entry: JournalEntry, retries: Int): Unit = try {
    val receiver = context.system.actorFor(entry.receiverevent.receiver)
    try {
      Await.result(receiver ? Exists, defaulttimeout.duration)
      receiver ! entry.receiverevent.event
    } catch {
      case err ⇒
        if (retriesduringredo * 0.67 > retries)
          if (0 == retries % 10) log.info("retry " + retries + " " + entry + " : " + err)
          else
            log.debug("retry " + retries + " " + entry + " : " + err)
        Thread.sleep(pauseduringredo)
        if (0 == retries) {
          log.error("redo failed " + entry)
          addInvalidLocation(new Location(entry.filedescriptor.toInt, entry.pointer.toInt))
        } else {
          publish(entry, retries - 1)
        }
    }
  } catch {
    case e ⇒
      log.error("publish failed : " + e)
  }

  private[this] lazy val syncInterval = schedule(disposeinterval, disposeinterval) {
    try {
      log.debug("sync : last " + synccounter + " current " + writecounter)
      if (synccounter.get < writecounter.get) {
        log.info("sync : entries written " + (writecounter.get - synccounter.get) + " last " + synccounter + " current " + writecounter)
        journal.sync
        synccounter.set(writecounter.get)
      }
    } catch {
      case e ⇒
        log.error("sync failed : last " + synccounter + " current " + writecounter + " reason : " + e)
    }
  }

  private[this] def doBackup(receiverevent: ReceiverEvent) = spawn {
    try {
      log.debug("doBackup " + writecounter)
      val out = new FileOutputStream(new File(xmlbackupdirectory, backupcounter.incrementAndGet + ".xml"))
      try {
        receiverevent.toXml(out)
      } catch {
        case e ⇒ log.warning("backup failed for " + receiverevent + "\n" + e)
      } finally {
        out.close
      }
    } catch {
      case e ⇒
        log.error("backup failed : " + e + " for " + receiverevent)
    }
  }

  private[this] val journal: JournalIO = try {
    var activated = false
    var journal: JournalIO = null
    var suffix = filesuffix
    while (!activated) try {
      datadirectory.mkdirs
      archivedirectory.mkdirs
      xmlbackupdirectory.mkdirs
      journal = new JournalIO
      journal.setDirectory(datadirectory)
      journal.setDirectoryArchive(archivedirectory)
      journal.setArchiveFiles(true)
      journal.setMaxFileLength(maxfilesize)
      journal.setMaxWriteBatchSize(maxbatchsize)
      journal.setDisposeInterval(disposeinterval)
      journal.setFilePrefix(fileprefix)
      journal.setFileSuffix(suffix)
      journal.setReplicationTarget(new JournalIOReplication(log, context.dispatcher))
      log.info("activating journal ...")
      journal.open
      activated = true
    } catch {
      case e ⇒
        log.error("activating failed\n" + stackTraceToString(e))
        if (null != journal) {
          val datafiles = journal.getFiles
          journal.close
          log.warning("datafiles will be renamed : " + datafiles)
          datafiles.foreach { file ⇒
            Thread.sleep(1000)
            val path = file.toPath
            try {
              val npath = java.nio.file.Files.move(path, path.resolveSibling(path.getFileName + ".corrupted"))
              log.warning("datafile renamed : " + npath)
            } catch {
              case e ⇒
                log.error(e.toString)
                log.error("Must switch to recovery mode. The datafiles must be checked and eventually repaired manually.")
                suffix = suffix + ".recovery"
            }
          }
          Thread.sleep(1000)
        }
    }
    log.info("journal active")
    syncInterval
    journal
  } catch {
    case e ⇒
      log.error("""
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
! Fatal error : Journal cannot be activated. Program will be aborted.                    !
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!""")
      core.terminateJvm(e, -1)
  }

  private[this] val redocounter = {
    val r = new AtomicLong
    if (!journal.getFiles.isEmpty) journal.redo(new Location(1, 1)).iterator.foreach { location ⇒
      r.incrementAndGet
      if (0 == r.get % 10000) log.info("count redo " + r)
      if (0 == r.get % 1000) log.debug("count redo " + r)
    }
    log.info("redo entries " + r)
    r
  }

  private[this] val writecounter = new AtomicLong(redocounter.get)

  private[this] val synccounter = new AtomicLong(redocounter.get)

  private[this] val backupcounter = new AtomicLong

  private[this] val invalidlocations = new collection.mutable.ListBuffer[Location]

}

/**
 * Replicate the journal to a journal at a different site.
 */
class JournalIOReplication(log: LoggingAdapter, dispatcher: MessageDispatcher) extends ReplicationTarget {

  replicatedirectory.mkdirs

  def replicate(location: Location, bytes: Array[Byte]) = spawn {
    try {
      if ((filedescriptor.get < location.getDataFileId) ||
        (filedescriptor.get == location.getDataFileId && position.get < location.getPointer)) {
        filedescriptor.set(location.getDataFileId)
        position.set(location.getPointer)
        log.debug("replicate [" + location + "] [" + bytes.length + " bytes]")
        val journal = journalfile(location.getDataFileId)
        journal.write(bytes)
        journal.close
      } else {
        log.error("""
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
! replication file could be corrupted by an unordered sequence, next sequence skipped    !
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!""")
        log.error("replication error : previous location " + filedescriptor + ":" + position + " is larger than next location " + location + ". Next location skipped.")
      }
    } catch {
      case e ⇒
        log.error("""
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
! replication file could be corrupted caused by an exception, next sequence skipped      !
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!""")
        log.error("replication error : previous location " + filedescriptor + ":" + position + " next location " + location + " skipped. Reason: " + e)
    }
  }

  private[this] def journalfile(fileid: Int) = {
    val filename = fileprefix + fileid + filesuffix
    new FileOutputStream(new java.io.File(replicatedirectory, filename), true)
  }

  private[this] val filedescriptor = new AtomicLong(-1L)

  private[this] val position = new AtomicLong(-1L)

}

