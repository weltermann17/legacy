package com.ibm.haploid

package dx

package engine

package domain

import java.io.File

import collection.JavaConversions._

import core.file.temporaryDirectory
import core.operatingsystem
import core.newLogger
import core.util.text.stackTraceToString

/**
 *
 */
package object operating {

  import core.config._

  val operatorclasses = try {
    (getConfigList("haploid.dx.engine.domain.operating.operator-classes").toList ++
      getConfigList("haploid.dx.engine.domain.operating.custom-operator-classes").toList)
      .map(c ⇒ OperatorClass(
        Class.forName(c.getString("operator-class")).asInstanceOf[Class[_ <: OperatorBase]],
        c.getString("name"),
        c.getInt("number-of-instances"),
        c.getMilliseconds("timeout"),
        { if (c.hasPath("repeat")) c.getInt("repeat") else 0 },
        { if (c.hasPath("repeat-timeout")) c.getMilliseconds("repeat-timeout") else 0 }))
      .toArray
      .asInstanceOf[Array[OperatorClass]]
  } catch {
    case e: Throwable ⇒
      newLogger(this).error(stackTraceToString(e))
      throw e
  }

  val rootdirectory = getDirectory("haploid.dx.engine.domain.operating.root-directory").toPath

  val maxloggingperoperationsize = getBytes("haploid.dx.engine.domain.operating.max-logging-per-operation-size").toInt

  val scriptbasecommandline = getString("haploid.dx.engine.domain.operating.script-base-command-line-" + operatingsystem).trim

  val scriptextension = getString("haploid.dx.engine.domain.operating.script-extension-" + operatingsystem)

  val consolecharset = getString("haploid.dx.engine.domain.operating.console-charset-" + operatingsystem)

  val encodeoutputwithbase64 = getBoolean("haploid.dx.engine.domain.operating.encode-output-with-base64 ")

  private[this] def getDirectory(d: String) = getString(d) match {
    case "temp" ⇒ temporaryDirectory
    case f ⇒ new File(f)
  }

}

