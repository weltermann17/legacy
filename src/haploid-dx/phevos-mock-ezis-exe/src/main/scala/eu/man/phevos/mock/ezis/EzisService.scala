package eu.man.phevos.mock.ezis

import com.ibm.haploid.rest.HaploidService
import com.ibm.haploid.rest.util.Authentification
import cc.spray.http.ContentType.fromMimeType
import cc.spray.http.HttpHeaders.CustomHeader
import cc.spray.http.MediaTypes._
import cc.spray.http.StatusCode.int2StatusCode
import cc.spray.RequestContext
import sun.misc.BASE64Decoder
import cc.spray.http.StatusCodes

class EzisService extends HaploidService {

  lazy val service = path("cgi-bin/phevos/get4phevos.pl") {
    auth {
      get {
        parameters("Prog", "User", "Typ", "Znr", "Bl", "Ind") { (prog, user, typ, znr, bl, ind) ⇒

          val header2 = CustomHeader("Pragma", "no-cache")
          val header4 = CustomHeader("Cache-control", "no-store")

          znr match {
            case x if (x == "33771158558" && bl == "01" && ind == "B") ⇒ {
              val filename = "33771158558_01_B_CX_pre.tif"
              val header = CustomHeader("Content-Disposition", "filename=" + filename)
              val header3 = CustomHeader("Content-Type", "name=" + filename)
              respondWithHeaders(header, header2, header3) {
                getFromResource(filename)
              }
            }
            case x if (x == "36712014070" && bl == "01" && ind == "A") ⇒ {
              val filename = "36712014070_01_A_CX_pre.tif"
              val header = CustomHeader("Content-Disposition", "filename=" + filename)
              val header3 = CustomHeader("Content-Type", "name=" + filename)
              respondWithHeaders(header, header2, header3) {
                getFromResource(filename)
              }
            }
            case x if (x == "36744068054" && bl == "01" && ind == "-") ⇒ {
              val filename = "36744068054_01___CX_pre.tif"
              val header = CustomHeader("Content-Disposition", "filename=" + filename)
              val header3 = CustomHeader("Content-Type", "name=" + filename)
              respondWithHeaders(header, header2, header3) {
                getFromResource(filename)
              }
            }
            case x if (x == "36764508084" && bl == "01" && ind == "A") ⇒ {
              val filename = "36764508084_01_A_CX_pre.tif"
              val header = CustomHeader("Content-Disposition", "filename=" + filename)
              val header3 = CustomHeader("Content-Type", "name=" + filename)
              respondWithHeaders(header, header2, header3) {
                getFromResource(filename)
              }
            }
            case x if (x == "36776200125" && bl == "01" && ind == "-") ⇒ {
              val filename = "36776200125_01___CX_pre.tif"
              val header = CustomHeader("Content-Disposition", "filename=" + filename)
              val header3 = CustomHeader("Content-Type", "name=" + filename)
              respondWithHeaders(header, header2, header3) {
                getFromResource(filename)
              }
            }
            case x if (x == "81123008066" && bl == "01" && ind == "C") ⇒ {
              val filename = "81123008066_01_C_CX_pre.tif"
              val header = CustomHeader("Content-Disposition", "filename=" + filename)
              val header3 = CustomHeader("Content-Type", "name=" + filename)
              respondWithHeaders(header, header2, header3) {
                getFromResource(filename)
              }
            }
            case x if (x == "81418048680" && bl == "01" && ind == "B") ⇒ {
              val filename = "81418048680_01_B_CX_pre.tif"
              val header = CustomHeader("Content-Disposition", "filename=" + filename)
              val header3 = CustomHeader("Content-Type", "name=" + filename)
              respondWithHeaders(header, header2, header3) {
                getFromResource(filename)
              }
            }
            case x if (x == "81418048680" && bl == "02" && ind == "B") ⇒ {
              val filename = "81418048680_02_B_CX_pre.tif"
              val header = CustomHeader("Content-Disposition", "filename=" + filename)
              val header3 = CustomHeader("Content-Type", "name=" + filename)
              respondWithHeaders(header, header2, header3) {
                getFromResource(filename)
              }
            }
            case _ ⇒ {
              respondWithContentType(`text/xml`) {
                respondWithStatus(400) {
                  respondWithHeaders(header4, header2) {
                    completeWith("<INFO>\n\t<MESSAGE>Fehler</MESSAGE>\n\t<TEXT>keine Datei</TEXT>\n</INFO>")
                  }
                }
              }
            }
          }
        }
      }
    }
  } ~
    (path("status") | path("help") | path("info")) {
      completeWith(helpText)
    }

  def isAuthentificationOk(ctx: RequestContext): Boolean = {
    val authHeader = ctx.request.headers.find({ header ⇒
      if (header.name.equals("Authorization")) true else false
    })

    if (authHeader != None) {
      val auth = authHeader.get
      val userpass = new String(new BASE64Decoder().decodeBuffer(auth.value.replace("Basic ", ""))).split(":")
      val username = userpass(0)
      val password = userpass(1)

      checkUserPassword(username, password)
    } else {
      false
    }
  }

  def checkUserPassword(username: String, password: String): Boolean = {
    username.equals("testuser") && password.equals("testpassword")
  }

  def auth(f: RequestContext ⇒ Unit) = get { ctx ⇒
    completeWithContext(ctx) {
      if (!isAuthentificationOk(ctx)) {
        respondWithStatus(StatusCodes.Unauthorized) {
          completeWith("Not authorized")
        }
      } else {
        f
      }
    }
  }

  val helpText = """
    Testuser: testuser
    Testpassword: testpassword
    
    Valid URLs are:
    http://localhost:8082/cgi-bin/phevos/get4phevos.pl?Prog=Test&User=u01wt&Typ=2d&Znr=33771158558&Bl=01&Ind=B
    http://localhost:8082/cgi-bin/phevos/get4phevos.pl?Prog=Test&User=u01wt&Typ=2d&Znr=36712014070&Bl=01&Ind=A
    http://localhost:8082/cgi-bin/phevos/get4phevos.pl?Prog=Test&User=u01wt&Typ=2d&Znr=36764508084&Bl=01&Ind=A
    http://localhost:8082/cgi-bin/phevos/get4phevos.pl?Prog=Test&User=u01wt&Typ=2d&Znr=81418048680&Bl=01&Ind=B
    http://localhost:8082/cgi-bin/phevos/get4phevos.pl?Prog=Test&User=u01wt&Typ=2d&Znr=81418048680&Bl=02&Ind=B
    http://localhost:8082/cgi-bin/phevos/get4phevos.pl?Prog=Test&User=u01wt&Typ=2d&Znr=36744068054&Bl=01&Ind=-
    http://localhost:8082/cgi-bin/phevos/get4phevos.pl?Prog=Test&User=u01wt&Typ=2d&Znr=36776200125&Bl=01&Ind=-
    http://localhost:8082/cgi-bin/phevos/get4phevos.pl?Prog=Test&User=u01wt&Typ=2d&Znr=81123008066&Bl=01&Ind=C
    """

}