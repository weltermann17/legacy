package eu.man.phevos.dx.engine.domain

package object jobs {

  import com.ibm.haploid.core.config._

  val collectJobTimeout = getMilliseconds("phevos.dx.engine.domain.jobs.collect-job-timeout")

}