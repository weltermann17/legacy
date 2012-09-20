package com.ibm.haploid

package core

package util

import scala.collection.JavaConversions.asScalaBuffer

package object logging {

  import config._

  val filterdebugloggernames: List[String] = try {
    getStringList("haploid.core.logging.filter-debug-logger-names").toList
  } catch {
    case _: Throwable ⇒ List.empty
  }

}

