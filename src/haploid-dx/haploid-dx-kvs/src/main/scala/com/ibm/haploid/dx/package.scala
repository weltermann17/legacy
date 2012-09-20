package com.ibm.haploid

package object dx {
  
  import core.config._

  val proxyHost = getString("haploid.dx.proxyHost")
  val proxyPort = getString("haploid.dx.proxyPort")
  val proxyUser = getString("haploid.dx.proxyUser")
  val proxyPassword = getString("haploid.dx.proxyPassword")

}