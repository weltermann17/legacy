package com.ibm.haploid

import org.junit.Test

@Test class HaploidSystemTest {

  @Test def test1 = {
    val haploid = HaploidSystem(getClass.getClassLoader)
    println("haploid started.")
    new Thread(new Runnable { def run = { Thread.sleep(2000); haploid.shutdown } }).start
    haploid.log.debug("This is a debug.")
    haploid.log.info("This is an info.")
    haploid.log.warning("This is a warning.")
    haploid.log.error("This is an error.")
    haploid.log.warning("This is a warning.")
    haploid.log.info("This is an info.")
    haploid.log.debug("This is a debug.")
    haploid.log.error("This is an error.")
    println(java.nio.file.Files.createTempFile(null, null))
    haploid.awaitTermination
  }

}

