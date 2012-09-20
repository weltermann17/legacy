package com.ibm.haploid

package dx

package engine

package domain

import java.io.File

import collection.JavaConversions._

import core.file.temporaryDirectory
import core.operatingsystem

/**
 *
 */
package object operating {

  import core.config._

  val operatorclasses = try {
    (getConfigList("haploid.dx.engine.domain.operating.operator-classes").toList ++
      getConfigList("haploid.dx.engine.domain.operating.custom-operator-classes").toList)
      .map(c => OperatorClass(
          Class.forName(c.getString("operator-class")).asInstanceOf[Class[_ <: OperatorBase]], 
          c.getString("name"), 
          c.getInt("number-of-instances"),
          c.getMilliseconds("timeout")))
      .toArray
      .asInstanceOf[Array[OperatorClass]]
  } catch {
    case e =>
      e.printStackTrace
      throw e
  }

  val rootdirectory = getDirectory("haploid.dx.engine.domain.operating.root-directory").toPath

  val maxloggingperoperationsize = getBytes("haploid.dx.engine.domain.operating.max-logging-per-operation-size").toInt

  val scriptbasecommandline = getString("haploid.dx.engine.domain.operating.script-base-command-line-" + operatingsystem).trim

  val scriptextension = getString("haploid.dx.engine.domain.operating.script-extension-" + operatingsystem)

  val consolecharset = getString("haploid.dx.engine.domain.operating.console-charset-" + operatingsystem)

  private[this] def getDirectory(d: String) = getString(d) match {
    case "temp" ⇒ temporaryDirectory
    case f ⇒ new File(f)
  }

} 

