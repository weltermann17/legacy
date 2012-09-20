package com.ibm.haploid

package core

package compiler

import org.junit.Assert.assertTrue
import org.junit.Test

import file._

private class CompilerTest {

  @Test def testCompilerInt = {
    val tmp = temporaryDirectory
    val f = temporaryFileInDirectory(tmp)
    val o = new java.io.PrintWriter(new java.io.FileWriter(f))
    o.println("1 + 4")
    o.close
    for (i <- 1 to 5) {
      val compiler = new Compiler(f)
      val j: Int = compiler()
      assertTrue(5 == j)
    }
  }

  @Test def testCompilerString = {
    val tmp = temporaryDirectory
    val f = temporaryFileInDirectory(tmp)
    val o = new java.io.PrintWriter(new java.io.FileWriter(f))
    o.println(""" "hello, " + "world!" """)
    o.close
    val compiler = new Compiler(f)
    val j = compiler[String]()
    assertTrue("hello, world!" == j)
  }

  @Test def testCompilerStringNoDir = {
    val f = temporaryFile
    val o = new java.io.PrintWriter(new java.io.FileWriter(f))
    o.println(""" "hello, " + "world!" """)
    o.close
    val compiler = new Compiler(f)
    val j: String = compiler()
    assertTrue("hello, world!" == j)
  }

  @Test def testCompilerStringNoSourceFile = {
    val tmp = temporaryDirectory
    val compiler = new Compiler(""" "hello, " + "world!" """)
    val j: String = compiler()
    assertTrue("hello, world!" == j)
  }
  
  @Test def testCompilerComplexConfig = {
    import com.typesafe.config.Config
    def test = {
      import akka.actor.ActorSystem
      import com.typesafe.config.ConfigFactory.parseString
      parseString("""
          akka.actor.deployment {
    		  /my-service {
    		  	router = round-robin
    		  	nr-of-instances = 3
    		  }
    	  }""")      
    }    
    val code = """
      import akka.actor.ActorSystem
      import com.typesafe.config.ConfigFactory.parseString
      parseString("akka.actor.deployment { /my-service { router = round-robin, nr-of-instances = 3 } }")      
    """
    val tmp = temporaryDirectory
    val compiler = new Compiler(code)
    val config: Config = compiler()
    assertTrue(test == config)
  }

}

