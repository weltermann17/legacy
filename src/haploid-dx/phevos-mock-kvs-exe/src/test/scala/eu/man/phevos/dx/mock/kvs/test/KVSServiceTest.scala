package eu.man.phevos.dx.mock.kvs.test
import com.ibm.haploid.rest.client.HaploidRestClient
import cc.spray.http.HttpCharsets._
import cc.spray.http.MediaTypes._
import cc.spray.http.ContentType
import cc.spray.http.HttpContent
import cc.spray.http.{ HttpRequest, HttpMethods }
import cc.spray.util._
import java.io.File
import java.io.FileInputStream
import com.ibm.haploid.rest.util.MultipartFormDataBuilder

object KVSServiceTest extends HaploidRestClient {

  val conduit = createConduit("localhost", 8083)

  val file = new File("c:/tmp/upload.png")
  var fis = new FileInputStream(file)
  var bytes = new Array[Byte](file.length().toInt)
  fis.read(bytes)

  val body1 = """--VFC-Plauen
Content-Disposition: form-data; name="name"

Michael
--VFC-Plauen
Content-Disposition: form-data; name="firstname"

Wellner
--VFC-Plauen
Content-Disposition: form-data; name="age"

23
--VFC-Plauen
Content-Disposition: form-data; name="file"; filename="avatar.png"
Content-Type: image/png

""".getBytes()

  val body2 = """
--VFC-Plauen--""".getBytes()

  val body = Array.concat(body1, bytes, body2)

  val httpContent = MultipartFormDataBuilder.apply
    .append("name", "Michael")
    .append("firstname", "Wellner")
    .append("age", "23")
    .append("file", "image/png", "c:/tmp/upload.png")
    .get

  //  val httpContent = HttpContent(ContentType(new `multipart/form-data`(Some("VFC-Plauen")), `UTF-8`), body)
  val request = HttpRequest(HttpMethods.GET, uri = "/data", content = Some(httpContent))
  val responseFuture = conduit.sendReceive(request)
  val response = responseFuture.await
  conduit.close()

  val s = response.content.get.toString()

  println(s)

  System.exit(0)
}