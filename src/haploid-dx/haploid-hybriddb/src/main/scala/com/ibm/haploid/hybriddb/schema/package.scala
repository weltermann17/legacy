package com.ibm.haploid

package hybriddb

/**
 *
 */
package object schema {

  import akka.util.duration.longToDurationLong

  import core.config._

  val pagecachetimeout = getMilliseconds("haploid.hybriddb.schema.column.page-cache-timeout").toLong.milliseconds
  
} 

