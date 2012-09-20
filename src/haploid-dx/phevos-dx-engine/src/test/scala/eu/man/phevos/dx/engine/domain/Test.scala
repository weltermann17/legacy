package eu.man

package phevos

package dx

package engine

package domain

import akka.actor._
import akka.dispatch._
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import org.junit.Test
import com.ibm.haploid.core.inject.BaseBindingModule
import com.ibm.haploid.core.concurrent.{ actorsystem ⇒ system } 
import com.ibm.haploid.dx.engine._
import com.ibm.haploid.dx.engine.domain._
import com.ibm.haploid.dx.engine.event._
import com.ibm.haploid.dx.engine.journal.journal
import akka.dispatch.OnFailure

@Test private class DomainTest {

  @Test def testAll = {
    for (i ← 1 to 1) {

      object binding extends BaseBindingModule({ module ⇒
        import module._
        bind[ActorRef] identifiedBy 'journal toSingle journal
      })

      val engine = EngineFSM(binding)

      (journal ? Redo)(Timeout(15 minutes)) onSuccess {

        case e ⇒

          Thread.sleep(1000); println("Online")

          for (i ← 1 to 100) {
            Await.result(journal ? ReceiverEvent(
              "/engine/jobs",
              JobCreate(classOf[JobFSM], JobDetail("65.4711-" + i, "___", "WHT." + i, ""))), defaulttimeout.duration)
          }

          Thread.sleep(20000); println("Created")

          val collector = system.actorOf(Props[Collector])

          engine ! Collect(collector)

          Thread.sleep(2000); println("Info")

          (collector ? Collect(null)).onSuccess { case e: Engine ⇒ e.toXml(System.out) }

          Thread.sleep(1000); println("Offline")

          system.shutdown

      } onFailure {
        case e ⇒
          println(e)
          system.shutdown
      }

      system.awaitTermination; println("End")

    }
  }

}