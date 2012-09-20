package com.ibm.haploid.disruptor

object AlertException extends Exception {

  override def fillInStackTrace = this
  
}

object TimeoutException extends Exception {

  override def fillInStackTrace = this
  
}
