package com.ibm.haploid

package dx

import scala.collection.JavaConversions.asScalaBuffer

import akka.util.Timeout

import engine.domain.{ JobClass, JobFSM }

import core.config.{ getMilliseconds, getConfigList }
import core.newLogger
import core.util.text.stackTraceToString

/**
 *
 */
package object engine {

  import core.config._

  implicit val defaulttimeout = Timeout(getMilliseconds("haploid.dx.engine.default-timeout"))

  val jobclasses = try {
    getConfigList("haploid.dx.engine.domain.jobs").toList.map(j ⇒
      (j.getString("name"), JobClass(
        Class.forName(j.getString("job-class")).asInstanceOf[Class[_ <: JobFSM]],
        j.getString("name"),
        { if (j.hasPath("continuous-job")) j.getBoolean("continuous-job") else false })))
      .toMap
  } catch {
    case e: Throwable ⇒
      newLogger(this).error(stackTraceToString(e))
      throw e
  }

  val continuousJobPaths = jobclasses.filter(_._2.continuous).map { case (name, _) ⇒ "akka://default/user/engine/jobs/" + name }.toList

}

