package com.ibm.haploid

package dx

package engine

import java.io.File
import akka.actor._
import core.concurrent.{ actorsystem ⇒ system }
import core.file.temporaryDirectory
import core.util.time.now
import java.nio.file.Paths

/**
 *
 */
package object journal {

  import core.config._

  lazy val journal = system.actorOf(
    Props(Class.forName(getString("haploid.dx.engine.journal.journal-class")).asInstanceOf[Class[_ <: Journal]])
      .withDispatcher("haploid.core.concurrent.haploid-core-pinned-dispatcher"),
    name = "journal")

  val fileprefix = getString("haploid.dx.engine.journal.fileprefix")

  val filesuffix = getString("haploid.dx.engine.journal.filesuffix")

  val datadirectory = getDirectory("haploid.dx.engine.journal.data-directory")

  val archivedirectory = getDirectory("haploid.dx.engine.journal.archive-directory")

  val xmlbackupdirectory = new File(getDirectory("haploid.dx.engine.journal.xml-backup-directory"), now.toString)
  
  val xmlbackupbasedirectory = Paths.get(getDirectory("haploid.dx.engine.journal.xml-backup-directory").toString)

  val replicatedirectory = getDirectory("haploid.dx.engine.journal.replicate-directory")

  val maxfilesize = getBytes("haploid.dx.engine.journal.max-filesize").toInt

  val maxbatchsize = getBytes("haploid.dx.engine.journal.max-batchsize").toInt

  val pauseduringredo = getMilliseconds("haploid.dx.engine.journal.pause-during-redo")

  val disposeinterval = getMilliseconds("haploid.dx.engine.journal.dispose-interval")

  val retriesduringredo = getInt("haploid.dx.engine.journal.retries-during-redo")

  val removeentryifretriesfail = getBoolean("haploid.dx.engine.journal.remove-entry-if-retries-fail")

  val logmodulo = getInt("haploid.dx.engine.journal.log-modulo")

  private[this] def getDirectory(d: String) = getString(d) match {
    case "temp" ⇒ temporaryDirectory
    case f ⇒ new File(f)
  }

}

