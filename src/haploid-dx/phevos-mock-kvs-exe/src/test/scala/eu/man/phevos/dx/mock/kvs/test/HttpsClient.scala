package eu.man.phevos.dx.mock.kvs.test
import java.security.KeyStore
import java.security.SecureRandom

import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.actor.Props
import cc.spray.can.client.HttpClient
import cc.spray.can.client.HttpDialog
import cc.spray.can.model.HttpRequest
import cc.spray.io.pipelines.ServerSSLEngineProvider
import cc.spray.io.IoWorker
import cc.spray.util.pimpFuture
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

object HttpsClient extends App {

  implicit val system = ActorSystem()

  implicit def sslContext: SSLContext = {
    val keyStoreResource = "/ssl-test-keystore.jks"
    val password = ""

    val keyStore = KeyStore.getInstance("jks")
    keyStore.load(getClass.getResourceAsStream(keyStoreResource), password.toCharArray)
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, password.toCharArray)
    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom)
    context
  }

  def log = system.log

  val ioWorker = new IoWorker(system).start()

  val httpClient = system.actorOf(
    props = Props(new HttpClient(ioWorker, ConfigFactory.parseString("spray.can.client.ssl-encryption = on"))),
    name = "https-client")

  val responseF = HttpDialog(httpClient, "localhost", port = 8080)
    .send(HttpRequest(uri = "/"))
    .end

  val response = responseF.await

  log.info(
    """Result from host: 
          	|status: {}
          	|headers: {}
          	|body: {} bytes""".stripMargin, response.status, response.headers.mkString("\n ", "\n ", ""), response.body.length)

  system.shutdown()
  ioWorker.stop()
}