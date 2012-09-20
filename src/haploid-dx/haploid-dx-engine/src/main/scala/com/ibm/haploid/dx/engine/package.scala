package com.ibm.haploid

package dx

import akka.util.Timeout

/**
 *
 */
package object engine { 

  import core.config._

  implicit val defaulttimeout = Timeout(getMilliseconds("haploid.dx.engine.default-timeout"))

} 

