package eu.man.phevos.dx.mock.kvs
import com.ibm.haploid.rest.HaploidService
import cc.spray.http.FormData
import cc.spray.http.MultipartFormData
import java.io.FileOutputStream
import java.io.File

class KVSService extends HaploidService {

  lazy val service = pathPrefix("DE-script/webagent") {
    parameters("sys_name", "sys_id") { (sys_name, sys_id) ⇒
      if (!sys_name.equals("MAN2KVS") || !sys_id.equals("A54DB2847F889573")) {
        completeWith("sys_name or sys_id incorrect")
      } else {
        pathPrefix("co.url_select") {
          path("docuvers") {
            parameters("TEIL_KEY") { teilKey ⇒
              if (teilKey.equals("T.EST.JSD.MAN.*"))
                completeWith(TestData.DOCUVERS_1)
              else
                reject()
            }
          } ~ path("/") {
            completeWith("TBD")
          }
        } ~ path("wp.checkin()") {
          completeWith(TestData.FILE_UPLOAD_ERROR)
        }
      }
    }

  }
  //https://docuvers?sys_name=MAN2KVS&sys_id=A54DB2847F889573&TEIL_KEY=T.EST.JSD.MAN.*&API2_INFO_TYPE=255
  //  lazy val service = path("kvs") {
  //    completeWith("I'm KVS")
  //  } ~ path("form") {
  //    formFields("name", "firstname", "age", "file".as[Array[Byte]]) { (name, firstname, age, file) =>
  //      completeWith(name + ", " + firstname + "," + age + "," + file.size)
  //    }
  //  } ~ path("data") {
  //    formFields("name", "firstname", "age") { (name, firstname, age) =>
  //      content(as[MultipartFormData]) { formData =>
  //        val file = formData.parts.get("file").get.content.get.buffer
  //        val stream = new FileOutputStream(new File("C:/tmp/download.file"))
  //        stream.write(file)
  //        stream.flush
  //        stream.close
  //        completeWith(name + ", " + firstname + "," + age + ", File written.")
  //      }
  //    }
  //  }

}