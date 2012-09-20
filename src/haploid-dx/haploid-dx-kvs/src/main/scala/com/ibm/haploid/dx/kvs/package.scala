package com.ibm.haploid

package dx

import java.io.{ FileOutputStream, File }

import com.ibm.haploid.core.config.{ getString, getInt }
import com.ibm.haploid.core.newLogger
import com.ibm.haploid.core.util.io.copyBytes

package object kvs {

  import com.ibm.haploid.core.config._

  val host = getString("haploid.dx.kvs.host")
  val port = getInt("haploid.dx.kvs.port")
  val username = getString("haploid.dx.kvs.username")
  val password = getString("haploid.dx.kvs.password")
  val truststore = getString("haploid.dx.kvs.truststore")

  private val truststoreFile = new File(truststore)
  if (!truststoreFile.exists) {
    truststoreFile.getParentFile.mkdirs
    copyBytes(this.getClass.getResourceAsStream("/truststore.jks"), new FileOutputStream(truststoreFile))
    newLogger(this).debug("copied keystore resource to " + truststoreFile)
  }

  val truststorePassword = getString("haploid.dx.kvs.truststore-password")
  val sysId = getString("haploid.dx.kvs.sys-id")
  val sysName = getString("haploid.dx.kvs.sys-name")

}
