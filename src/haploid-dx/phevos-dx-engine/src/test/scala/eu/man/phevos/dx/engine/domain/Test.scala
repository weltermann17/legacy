package eu.man

package phevos

package dx

package engine

package domain

import org.junit.Test
import com.ibm.haploid.core.concurrent.{ actorsystem ⇒ system }
import com.ibm.haploid.core.inject.BaseBindingModule
import dx.engine.domain.jobs.JobFSM
import com.ibm.haploid.dx.engine.domain.{ EngineFSM, Engine, Collector }
import com.ibm.haploid.dx.engine.event.{ Redo, ReceiverEvent, JobCreate, Collect }
import com.ibm.haploid.dx.engine.journal.journal
import com.ibm.haploid.dx.engine.defaulttimeout
import akka.actor.actorRef2Scala
import akka.actor.{ Props, ActorRef }
import akka.dispatch.Await
import akka.pattern.ask
import akka.util.duration.intToDurationInt
import akka.util.Timeout
import eu.man.phevos.dx.engine.domain.jobs.CollectJobFSM
import eu.man.phevos.dx.engine.domain.jobs.JobDetail

@Test private class DomainTest {

  import com.ibm.haploid.core.config._

  @Test def appDir = {
    println(getString("haploid.core.application-directory"))
  }

  //  @Test def testFlow = {
  //    object binding extends BaseBindingModule({ module ⇒
  //      import module._
  //      bind[ActorRef] identifiedBy 'journal toSingle journal
  //    })
  //
  //    val engine = EngineFSM(binding)
  //
  //    (journal ? Redo)(Timeout(15 minutes)) onSuccess {
  //
  //      case e: Throwable ⇒
  //
  //        Thread.sleep(50000); println("Created. Sleep 30 seconds.")
  //
  //        val collector = system.actorOf(Props[Collector])
  //
  //
  //        (collector ? Collect(engine)).onSuccess { case e: Engine ⇒ e.toXml(System.out) }
  //
  //        Thread.sleep(3000); println("Offline. Sleep 1 second.")
  //
  //        system.shutdown
  //    } onFailure {
  //      case e: Throwable ⇒
  //        println(e)
  //        system.shutdown
  //    } 
  //
  //    system.awaitTermination; println("End")
  //  }

  //  @Test def testAll = {
  //    for (i ← 1 to 1) {
  //
  //      object binding extends BaseBindingModule({ module ⇒
  //        import module._
  //        bind[ActorRef] identifiedBy 'journal toSingle journal
  //      })
  //
  //      val engine = EngineFSM(binding)
  //
  //      (journal ? Redo)(Timeout(15 minutes)) onSuccess {
  //
  //        case e: Throwable ⇒
  //
  //          Thread.sleep(1000); println("Online. Sleep 1 second.")
  //
  //          for (i ← 1 to 1) {
  //            Await.result(journal ? ReceiverEvent(
  //              "/engine/jobs",
  //              JobCreate(classOf[JobFSM], JobDetail("65.4711-" + i, "___", "WHT." + i, "", "03", "...", -1L, true, false))), defaulttimeout.duration)
  //          }
  //
  //          Thread.sleep(5000); println("Created. Sleep 5 secons.")
  //
  //          val collector = system.actorOf(Props[Collector])
  //
  //          engine ! Collect(collector)
  //
  //          Thread.sleep(1000); println("Info. Sleep 1 second.")
  //
  //          (collector ? Collect(null)).onSuccess { case e: Engine ⇒ e.toXml(System.out) }
  //
  //          Thread.sleep(1000); println("Offline. Sleep 1 second.")
  //
  //          system.shutdown
  //
  //      } onFailure {
  //        case e: Throwable ⇒
  //          println(e)
  //          system.shutdown
  //      }
  //
  //      system.awaitTermination; println("End")
  //
  //    }
  //  }

}