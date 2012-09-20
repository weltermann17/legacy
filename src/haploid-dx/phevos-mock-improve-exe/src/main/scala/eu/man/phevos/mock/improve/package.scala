package eu.man

package phevos

package mock

/**
 *
 */
package object improve {

  import com.ibm.haploid.core.config._

  val ftp_port = getInt("phevos.improve.ftp.port")
  val ftp_passiveports = getString("phevos.improve.ftp.passive-ports")
  val ftp_user = getString("phevos.improve.ftp.user")
  val ftp_password = getString("phevos.improve.ftp.password")

  val ftp_rootfolder = getString("phevos.improve.ftp.root-dir")
  val ftp_workingfolder = getString("phevos.improve.ftp.working-folder")
}

