package eu.man

package phevos

package dx

package engine

package domain

import com.ibm.haploid.core.config.getString
import com.ibm.haploid.core.{ operatingsystem, machinename }

/**
 *
 */
package object operating {

  import com.ibm.haploid.core.config._

  val pathtocatstart = try { getString("phevos.dx.engine.domain.operating.path-to-catstart-" + operatingsystem + "-" + machinename) } catch { case _ ⇒ "invalid" }

}

