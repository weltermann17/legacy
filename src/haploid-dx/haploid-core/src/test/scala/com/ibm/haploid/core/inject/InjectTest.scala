package com.ibm.haploid

package core

package inject

import org.junit.Assert.assertTrue
import org.junit.Test

@Test private class InjectTest {

  def printIf(s: String) = if (false) println(s)
  
  @Test def testBasic = {
    new SimpleUseConfiguration(1)(TestConfiguration)
    new BaseClass
    new ThisHasNoInjectionInItsSignature
    new AnyOtherClass
    ()
  }
  
  trait AnotherBaseClass extends Injectable {
    val bindingModule = TestConfiguration
  }
  
  /**
   * This is the recommended usage of 'CanInject'. 
   */
  class AnyOtherClass extends AnotherBaseClass {
    private val maxpoolsize = injectOptional[Int]('maxPoolSize) getOrElse 0
    private val connect = injectOptional[String]('connectString) getOrElse ""
    printIf("anyotherclass maxpoolsize " + maxpoolsize + " connect " + connect)
  }

  class SimpleUseConfiguration(val i: Int)(implicit val bindingModule: BindingModule) extends Injectable {
    private val maxpoolsize = injectOptional[Int]('maxPoolSize) getOrElse -1
    printIf("simple maxpoolsize " + maxpoolsize)
  }

  trait InjectableRoot extends Injectable {
    val bindingModule = TestConfiguration
  }

  class BaseClass extends InjectableRoot {
    private val maxpoolsize = inject[Int]('maxPoolSize)
    printIf("advanced maxpoolsize " + maxpoolsize)
  }

  class ThisHasNoInjectionInItsSignature extends BaseClass {
    private val maxpoolsize = inject[Int]('maxPoolSize)
    printIf("elaborated maxpoolsize " + maxpoolsize)
  }

  object TestConfiguration extends BaseBindingModule({ module =>
    import module._
    bind[Int] identifiedBy 'maxPoolSize toSingle 16
    bind[String] identifiedBy 'connectString toSingle "This is the connectstring."
    printIf("building TestConfiguration")
  })

}

