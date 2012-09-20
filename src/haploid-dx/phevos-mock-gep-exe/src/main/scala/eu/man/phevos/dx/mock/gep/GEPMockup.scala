package eu.man.phevos.dx.mock.gep
import com.ibm.haploid.rest.HaploidService
import cc.spray.directives.PathElement
import cc.spray.http.HttpHeaders.CustomHeader
import cc.spray.RequestContext

class GEPMockup extends HaploidService with GEPAuthentification {

  lazy val service = {
    pathPrefix("plm/divisions/truck/subsystems/enovia5") {
      pathPrefix("versions") {
        path("") {
          parameters("from".as[Int], "to".as[Int]) { (from, to) ⇒
            auth { ctx ⇒
              completeWithContext(ctx) {
                if (ctx.request.query.contains("83.28205-8530") && from == 0 && to == 50) {
                  completeWith(TestData.version_2)
                } else if (ctx.request.query.contains("81.93030-2081") && from == 0 && to == 50) {
                  completeWith(TestData.version_1)
                } else {
                  println(ctx.request.query, from, to)
                  reject()
                }
              }
            }
          }
        } ~ path(PathElement / "formats/summary") { id ⇒
          auth {
            if (id.equals("5EB7D5820098007C4A2FB7170009A957"))
              completeWith(TestData.summary)
            else if (id.equals("66D4EC02029000444F42656C000CA7D8"))
              completeWith(TestData.summary2)
            else
              completeWith(id)

          }
        }
      } ~ path("vaults/muc/nativeformats/jpg" / PathElement / PathElement / PathElement) { (s1, s2, s3) ⇒
        respondWithHeader(CustomHeader("Content-Disposition", "attachment; filename=81_93030_2081_G3D_0001____.jpg; size=-1")) {
          //          getFromFileName("C:/Temp/81_93030_2081_G3D_0001____.jpg") // TODO From Resource
          getFromResource("81_93030_2081_G3D_0001____.jpg")
        } ~ authentification
      }
    } ~
      path("plm/ezis/drawings/83282058530/H/1/tiff/38335F32383230355F383533305F4732445F303030315F5F485F2E74696666") {
        auth {
          respondWithHeader(CustomHeader("Content-Disposition", "attachment; filename=83_28205_8530_G2D_0001__H_.tiff; size=-1")) {
            // getFromFileName("C:/Temp/83_28205_8530_G2D_0001__H_.tiff") // TODO From Resource
            getFromResource("83_28205_8530_G2D_0001__H_.tiff")
          }
        }
      } ~
      path("auth") {
        auth {
          completeWith("Ok")
        }
      } ~
      path("large") { ctx ⇒
        completeWithContext(ctx) {
          val c: RequestContext = ctx

          def func: String = {
            "C:/tmp/XX_MOM00_0337_PRC" + ".CATProduct.jar"
          }

          getFromFileName(func)
        }
      } ~
      path("test") {
        getFromFileName("C:/tmp/XX_MOM00_0337_PRC.CATProduct2.jar")
      } ~
      path("small") {
        getFromFileName("C:/tmp/test.png")
      } ~ path("status") {
        completeWith("Running ...")
      }
  }

}