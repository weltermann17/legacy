package eu.man.phevos.dx.engine.domain

import com.ibm.haploid.core.service.Failure
import com.ibm.haploid.core.service.Result

package object exceptions {

  implicit def fromResultToPhevosException(ex: PhevosException): Result[Any] = Failure(ex)

}