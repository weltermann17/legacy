package com.ibm.haploid

package dx

package engine

package journal

import java.io.{ FileOutputStream, File }
import java.util.concurrent.atomic.AtomicLong
import javax.xml.bind.annotation.{ XmlType, XmlRootElement }
import scala.collection.JavaConversions._
import akka.actor.{ Cancellable, ActorPath, Props }
import akka.dispatch.{ MessageDispatcher, Await }
import akka.event.LoggingAdapter
import akka.pattern.ask
import core.concurrent.{ spawn, schedule }
import core.util.text.stackTraceToString
import event.Serialization.{ serialize, deserialize }
import event.{ ReceiverEvent, Exists }
import _root_.journal.io.api.{ Location, Journal ⇒ JournalIO, ReplicationTarget }
import akka.actor.ActorRef
import com.ibm.haploid.dx.engine.event.ExecutionCreate
import scala.collection.mutable.HashMap
import com.ibm.haploid.dx.engine.event.ExecutionResult
import java.nio.file.Files
import javax.xml.bind.JAXBContext
import com.ibm.haploid.dx.engine.domain.marshalling.Unmarshal
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.io.FilenameFilter
import java.nio.file.StandardCopyOption

/**
 *
 */
class JournalIOJournal

  extends Journal

  with Publisher {

  override protected def finalize = {
    syncInterval.cancel
    journal.sync
    journal.close
  }

  protected[this] def redo(sender: ActorRef) = redo(1, 1, sender)

  protected[this] def doAppend(receiverevent: ReceiverEvent): Unit = try {
    continuousJobPaths.find(receiverevent.receiver.toString.startsWith(_)) match {
      case Some(s) ⇒
        publishWhileAppend(JournalEntry(
          -1,
          -1,
          0L,
          receiverevent), retriesduringredo)
      case None ⇒
        publishWhileAppend(writeToJournal(receiverevent), retriesduringredo)
        doBackup(receiverevent)
    }
  } catch {
    case e: OutOfMemoryError ⇒ core.terminateJvm(e, -1)
    case e: Throwable ⇒
      log.error("append failed  " + writecounter + " : " + e)
      writecounter.decrementAndGet
  }

  private[this] def writeToJournal(receiverevent: ReceiverEvent): JournalEntry = {
    writecounter.incrementAndGet
    if (0 == writecounter.get % logmodulo) log.info("append " + writecounter)
    val location = journal.write(serialize(receiverevent), JournalIO.WriteType.SYNC)
    val entry = JournalEntry(
      location.getDataFileId,
      location.getPointer,
      writecounter.get,
      receiverevent)
    log.debug("append " + entry)
    entry
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
          case e: Throwable ⇒
            log.error("redo skipped entry " + readcounter + " / " + redocounter + " at location " + location + " : " + stackTraceToString(e))
            addInvalidLocation(location)
            None
        }
      }
    }
  } catch {
    case e: Throwable ⇒
      log.error("iterator failed : " + stackTraceToString(e)); nulliterator
  }

  private[this] object nulliterator extends Iterator[Option[JournalEntry]] {
    def hasNext = false
    def next = throw new NoSuchElementException
  }

  private[this] def redo(filedescriptor: Long, entry: Long, sender: ActorRef) = try {
    val relaunchSubscribes: HashMap[ActorPath, Option[JournalEntry]] = HashMap()

    iterator(filedescriptor, entry).foreach { entry ⇒
      entry match {
        case Some(entry) ⇒
          log.debug("redo " + entry)

          if (entry.receiverevent.event.isInstanceOf[ExecutionCreate]) {
            val path = entry.receiverevent.receiver./(entry.receiverevent.event.asInstanceOf[ExecutionCreate].name)
            relaunchSubscribes.put(path, None)
          } else if (entry.receiverevent.event.isInstanceOf[ExecutionResult]) {
            val path = entry.receiverevent.event.asInstanceOf[ExecutionResult].sender
            relaunchSubscribes.remove(path)
          }

          publishWhileRedo(entry, retriesduringredo)

          if (relaunchSubscribes.contains(entry.receiverevent.receiver.toString)) {
            relaunchSubscribes.update(entry.receiverevent.receiver, Some(entry))
          }

        case None ⇒
      }
    }

    relauncher ! Subscribers(relaunchSubscribes)

    removeInvalidLocations
    log.info("redo completed " + redocounter)
    sender ! ()
  } catch {
    case e: Throwable ⇒
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
          case e: Throwable ⇒
            log.error("remove location failed " + location + " : " + stackTraceToString(e))
        }
      }
      try { journal.sync; journal.delete(marker); journal.sync } catch { case e: Throwable ⇒ }
      invalidlocations.clear
    }
  } catch {
    case e: Throwable ⇒
      log.error("remove invalid locations failed : " + stackTraceToString(e))
  }

  private[this] def publishWhileAppend(entry: JournalEntry, maxretries: Int): Unit = {
    publish(entry.receiverevent.receiver, entry.receiverevent.event, maxretries, Some({ retries ⇒
      if (retries == 0) {
        log.debug("publish while append failed " + entry)
      }
    }))
  }

  private[this] def publishWhileRedo(entry: JournalEntry, maxretries: Int): Unit = {
    publish(entry.receiverevent.receiver, entry.receiverevent.event, maxretries, Some({ retries ⇒
      if (retries == 0) {
        log.error("publish while redo failed " + entry)
        addInvalidLocation(new Location(entry.filedescriptor.toInt, entry.pointer.toInt))
      }
    }))
  }

  private[this] lazy val syncInterval: Cancellable = schedule(disposeinterval, disposeinterval) {
    try {
      if (synccounter.get < writecounter.get) {
        log.info("sync : entries written " + (writecounter.get - synccounter.get) + " last " + synccounter + " current " + writecounter)
        journal.sync
        synccounter.set(writecounter.get)
      }
    } catch {
      case e: Throwable ⇒
        log.error("sync failed : last " + synccounter + " current " + writecounter + " reason : " + e)
    }
  }

  private[this] def doBackup(receiverevent: ReceiverEvent) = spawn {
    try {
      val out = new FileOutputStream(new File(xmlbackupdirectory, backupcounter.incrementAndGet + ".xml"))
      try {
        receiverevent.toXml(out)
        out.flush
      } catch {
        case e: javax.xml.bind.MarshalException ⇒ log.warning("backup failed for " + receiverevent + "\n" + e)
        case e: Throwable ⇒ log.warning("backup failed for " + receiverevent + "\n" + e)
      } finally {
        out.close
      }
    } catch {
      case e: Throwable ⇒
        log.error("backup failed : " + e + " for " + receiverevent)
    }
  }

  private[this] def readXmlBackup = {
    try {
      Files.newDirectoryStream(xmlbackupbasedirectory).toList.map(dir ⇒ (dir.toFile.getName.toLong, dir)).sortBy(_._1).map(_._2).foldLeft[List[ReceiverEvent]](List.empty) { (list, backup) ⇒
        list ++ (Files.newDirectoryStream(backup).toList.map(xmlFile ⇒ (xmlFile.toFile.getName.replace(".xml", ""), xmlFile)).toList.sortBy(_._1.toInt).map(_._2).map { path ⇒
          try {
            Unmarshal(path.toFile).asInstanceOf[ReceiverEvent]
          } catch {
            case e: Exception ⇒
              log.error("unable to unmarshal " + path)
              throw e
          }
        } toList)
      }
    } catch {
      case e: Exception ⇒
        log.error("error during xml backup: " + e)
        List.empty
    }
  }

  private[this] val relauncher = context.actorOf(Props(new Relauncher), name = "relauncher")

  private[this] val recoverySuffix = ".recovery"
  private[this] val corruptedSuffix = ".corrupted"

  private[this] val journal: JournalIO = try {

    var activated = false
    var journal: JournalIO = null
    var suffix = filesuffix

    datadirectory.mkdirs
    archivedirectory.mkdirs
    xmlbackupdirectory.mkdirs

    val datadirFiles = Files.newDirectoryStream(FileSystems.getDefault.getPath(datadirectory.getAbsolutePath)).toList
    datadirFiles.find(_.toString.endsWith(recoverySuffix)) match {
      case Some(_) ⇒
        log.warning("datafiles will be renamed : " + datadirFiles + "; replacing existing if neccessary")
        datadirFiles.filter(_.toString.endsWith(".data")).foreach { path ⇒
          val npath = Files.move(path, path.resolveSibling(path.getFileName + corruptedSuffix), StandardCopyOption.REPLACE_EXISTING)
          log.warning("datafile renamed to: " + npath)
        }

        datadirFiles.filter(_.toString.endsWith(recoverySuffix)).foreach { file ⇒
          val npath = Files.move(file, FileSystems.getDefault.getPath(file.toString.substring(0, file.toString.length - ".recovery".length)))
          log.warning("recovery file renamed to: " + npath)
        }
      case None ⇒
    }

    while (!activated) try {
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
      case e: Throwable ⇒
        log.error(e.toString)
        log.error("Must switch to recovery mode. Journal will be restored using xml-backup.")
        suffix = suffix + recoverySuffix
        Thread.sleep(1000)
    }

    log.info("journal active")
    journal
  } catch {
    case e: Throwable ⇒
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
    syncInterval
    r
  }

  private[this] val writecounter = new AtomicLong(redocounter.get)

  private[this] val synccounter = new AtomicLong(redocounter.get)

  private[this] val backupcounter = new AtomicLong

  private[this] val invalidlocations = new collection.mutable.ListBuffer[Location]

  if (journal.getFileSuffix.endsWith(recoverySuffix))
    try {
      log.info("starting xml-backup")
      readXmlBackup foreach { event ⇒
        println("backup event: " + event)
        writeToJournal(event)
      }
      log.info("xml-backup finshed")
    } catch {
      case e: Exception ⇒
        log.error("exception during xml-backup: " + e)
        throw e
    }

}

/**
 * Replicate the journal to a journal at a different site.
 */
class JournalIOReplication(log: LoggingAdapter, dispatcher: MessageDispatcher) extends ReplicationTarget {

  replicatedirectory.mkdirs

  def replicate(location: Location, bytes: Array[Byte]) = spawn {
    var journal: FileOutputStream = null
    try {
      if ((filedescriptor.get < location.getDataFileId) ||
        (filedescriptor.get == location.getDataFileId && position.get < location.getPointer)) {
        filedescriptor.set(location.getDataFileId)
        position.set(location.getPointer)
        log.debug("replicate [" + location + "] [" + bytes.length + " bytes]")
        journal = journalfile(location.getDataFileId)
        journal.write(bytes)
        journal.flush
      } else {
        log.error("""
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
! replication file could be corrupted by an unordered sequence, next sequence skipped    !
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!""")
        log.error("replication error : previous location " + filedescriptor + ":" + position + " is larger than next location " + location + ". Next location skipped.")
      }
    } catch {
      case e: Throwable ⇒
        log.error("""
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
! replication file could be corrupted caused by an exception, next sequence skipped      !
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!""")
        log.error("replication error : previous location " + filedescriptor + ":" + position + " next location " + location + " skipped. Reason: " + e)
    } finally {
      if (null != journal) journal.close
    }
  }

  private[this] def journalfile(fileid: Int) = {
    val filename = fileprefix + fileid + filesuffix
    new FileOutputStream(new java.io.File(replicatedirectory, filename), true)
  }

  private[this] val filedescriptor = new AtomicLong(-1L)

  private[this] val position = new AtomicLong(-1L)
}

