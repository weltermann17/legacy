package eu.man.phevos.dx.gep.test
import akka.actor.ActorSystem
import akka.actor.Props
import cc.spray.can.client.{ HttpClient, HttpDialog }
import cc.spray.io.IoWorker
import cc.spray.can.model.HttpRequest
import cc.spray.util._
import akka.actor.Actor

object SprayCan extends App {

  implicit val system = ActorSystem()
  def log = system.log

  val ioWorker = new IoWorker(system).start()
  val httpClient = system.actorOf(
    props = Props(new HttpClient(ioWorker)),
    name = "http-client")

  log.info("Dispatching GET request to github.com")

  val responseF =
    HttpDialog(httpClient, "localhost", 8082).send(HttpRequest(uri = "/large")).end

  val response = responseF.await
  println(response.bodyAsString)

  system.shutdown()
  System.exit(0)
}