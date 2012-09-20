package eu.man

package phevos

package dx

/**
 *
 */
package object util {

  import com.ibm.haploid.core.config._

  val dummy = getString("phevos.dx.util.dummy")
  val ftp_url = getString("phevos.dx.util.ftp.server")
  val ftp_port = getInt("phevos.dx.util.ftp.port")
  val ftp_login = getString("phevos.dx.util.ftp.user")
  val ftp_password = getString("phevos.dx.util.ftp.password")

} 

