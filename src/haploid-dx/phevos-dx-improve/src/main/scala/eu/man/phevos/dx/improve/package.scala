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

  val site = getString("phevos.dx.improve.ticket.site")
  val status = getString("phevos.dx.improve.ticket.status")
  val project = getString("phevos.dx.improve.ticket.project")

  val openpriority = getString("phevos.dx.improve.ticket.open.priority")

  val closepriority = getString("phevos.dx.improve.ticket.close.priority")

  val errcodeprefix = getString("phevos.dx.improve.ticket.error-code-prefix")
}

