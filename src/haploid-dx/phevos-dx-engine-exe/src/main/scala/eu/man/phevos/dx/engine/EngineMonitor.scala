package eu.man.phevos

package dx

package engine

import scala.annotation.implicitNotFound
import com.ibm.haploid.dx.engine.domain.{ Engine ⇒ EngineRes }
import com.ibm.haploid.core.concurrent.{ actorsystem ⇒ system }
import com.ibm.haploid.dx.engine.domain.Collector
import com.ibm.haploid.dx.engine.event.Collect
import com.ibm.haploid.dx.engine.defaulttimeout
import com.ibm.haploid.rest.HaploidService
import com.ibm.haploid.core._
import akka.actor.Props
import akka.dispatch.Await
import akka.pattern.ask
import akka.util.duration.intToDurationInt
import cc.spray.directives
import java.io.OutputStream
import cc.spray.directives.PathEnd
import cc.spray.directives.Remaining
import com.ibm.haploid.dx.engine.domain.Monitor
import com.ibm.haploid.dx.engine.domain.marshalling.Marshaled
import cc.spray.http.MediaTypes._

class StringOutputStream extends OutputStream {

  val mBuf = new StringBuilder

  def write(b: Int) = mBuf.append(b.asInstanceOf[Char])

  def getString = mBuf.toString

}

class EngineMonitor extends HaploidService {

  import EngineMonitor._

  lazy val service =

    path("logging") {
      getFromFileName(logfilehtml)
    } ~ pathPrefix("status" / Remaining) { path ⇒
      parameters('ct ? "xml", 'depth.as[Int]?) { (ct, depth) ⇒
        ctx ⇒
          completeWithContext(ctx) {
            val json = ctx.request.acceptedMediaRanges.find(m ⇒
              m.matches(`application/json`) && m.mainType != "*").isDefined

            lazy val engine = system.actorFor("akka://default/user/" + path)

            try {
              val collector = system.actorOf(Props[Collector])
              
              Await.result(collector ? Collect(engine, depth), 15 seconds) match {
                case e: Marshaled ⇒
                  val os = new StringOutputStream
                  if (json || ct.equals("json")) {
                    e.toJson(os)
                    respondWithMediaType(`application/json`) {
                      completeWith(os.getString)
                    }
                  } else {
                    e.toXml(os)
                    respondWithMediaType(`text/xml`) {
                      completeWith(os.getString)
                    }
                  }
              }
            } catch {
              case e: Throwable ⇒
              	e.printStackTrace
                completeWith("Object not found.")
            }
          }
      }
    }

}

object EngineMonitor {

  val monitorcounter = System.currentTimeMillis().toString

}