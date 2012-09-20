package com.ibm.haploid

package dx

package engine

import collection.JavaConversions._
import akka.util.Timeout
import core.newLogger
import core.util.text.stackTraceToString

/**
 *
 */
package object domain {

  import core.config._

  val infotimeout = Timeout(getMilliseconds("haploid.dx.engine.domain.info-timeout"))

  val infomaxduration = infotimeout.duration

  implicit val executorcontext = core.concurrent.actorsystem.dispatcher

  val pauseduringcreate = getMilliseconds("haploid.dx.engine.domain.pause-during-create")

  val retriesduringcreate = getInt("haploid.dx.engine.domain.retries-during-create")

  val taskclasses = try {
    getConfigList("haploid.dx.engine.domain.tasks").toList.map(c ⇒
      (c.getString("name"), TaskClass(
        Class.forName(c.getString("task-class")).asInstanceOf[Class[_ <: TaskFSM]],
        c.getString("name"))))
      .toMap + ("job-sequence-task" -> TaskClass(classOf[JobSequenceTaskFSM], "job-sequence-task"))
  } catch {
    case e: Throwable ⇒
      newLogger(this).error(stackTraceToString(e))
      throw e
  }
  
  val removejobdirectoryonsuccess = getBoolean("haploid.dx.engine.domain.remove-job-directory-on-success")

  val removejobdirectoryonfailure = getBoolean("haploid.dx.engine.domain.remove-job-directory-on-failure")

}

