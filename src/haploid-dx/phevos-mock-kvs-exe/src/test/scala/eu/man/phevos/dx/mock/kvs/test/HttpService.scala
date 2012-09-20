package eu.man.phevos.dx.mock.kvs.test
import akka.actor.ActorLogging
import akka.actor.Actor
import cc.spray.can.model._

class HttpService extends Actor with ActorLogging {
  import HttpMethods._

  protected def receive = {
    case HttpRequest(GET, "/", _, _, _) ⇒
      sender ! index
  }

  lazy val index = HttpResponse(
    headers = List(HttpHeader("Contentr-Type", "text/plain")),
    body = "Hello World".getBytes("UTF-8"))

}