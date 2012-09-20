package eu.man

package phevos

package dx

import com.ibm.haploid.core.newLogger
import scala.collection.JavaConversions._

/**
 *
 */
package object util {

  import com.ibm.haploid.core.config._

  val ftp_server = getString("phevos.dx.util.ftp.server")
  val ftp_port = getInt("phevos.dx.util.ftp.port")
  val ftp_user = getString("phevos.dx.util.ftp.user")
  val ftp_password = getString("phevos.dx.util.ftp.password")
  
  val eeParts = try {
  	getStringList("phevos.dx.util.eeParts").toList
  } catch {
    case e: Exception =>
      newLogger(this).error("Couldn't parse List phevos.dx.util.eeParts")
      List.empty
  }

}

