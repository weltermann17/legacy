package eu.man

package phevos

package dx

/**
 *
 */
package object improve {

  import com.ibm.haploid.core.config._

  val ftp_port = getInt("phevos.dx.improve.ftp.port")
  val ftp_user = getString("phevos.dx.improve.ftp.user")
  val ftp_password = getString("phevos.dx.improve.ftp.password")
  val ftp_workingfolder = getString("phevos.dx.improve.ftp.working-folder")
  val ftp_server = getString("phevos.dx.improve.ftp.server")
} 

