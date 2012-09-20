package com.ibm.haploid

package dx

package engine

package domain

import javax.xml.bind.{SchemaOutputResolver, JAXBContext}
import javax.xml.transform.stream.StreamResult

import akka.actor.ActorRef
import akka.pattern.ask

import org.junit.Test

import core.inject.BaseBindingModule

import event._
import journal.journal
import test._

@Test private class DomainTest {

  @Test def testAll = {
    for (i <- 1 to 1) {
      
      implicit object binding extends BaseBindingModule({ module =>
        import module._
        bind[ActorRef] identifiedBy 'journal toSingle journal
      })

      val engine = EngineFSM.apply

      journal ! Redo

      Thread.sleep(5000)

      for (i <- 1 to 10) journal ! JobCreate(classOf[JobFSM], JobNoDetail("nodetail"))

      Thread.sleep(5000)

      Thread.sleep(500)
    }
  }

}

