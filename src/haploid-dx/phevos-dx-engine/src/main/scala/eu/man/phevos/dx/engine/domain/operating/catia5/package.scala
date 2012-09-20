package eu.man

package phevos

package dx

package engine

package domain

package operating

/**
 *
 */
package object catia5 {

  import com.ibm.haploid.core.config._

  val server = getString("phevos.dx.engine.domain.operating.catiav5.server")
  
  val user = getString("phevos.dx.engine.domain.operating.catiav5.user")

  val role = getString("phevos.dx.engine.domain.operating.catiav5.role")
  
  val passwd = getString("phevos.dx.engine.domain.operating.catiav5.password")
  
} 

