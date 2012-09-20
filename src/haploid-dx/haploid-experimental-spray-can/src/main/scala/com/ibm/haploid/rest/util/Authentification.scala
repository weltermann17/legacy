package com.ibm.haploid.rest.util
import com.ibm.haploid.core.util.Uuid
import com.ibm.haploid.rest.HaploidService

import cc.spray.http.HttpHeaders.CustomHeader
import cc.spray.RequestContext
import sun.misc.BASE64Decoder

trait Authentification extends HaploidService {

  def checkUserPassword(username: String, password: String): Boolean

  val authentification = head { ctx ⇒
    completeWithContext(ctx) {
      if (isAuthentificationOk(ctx)) {
        val token = scala.util.Random.nextLong.toString
        respondWithHeader(CustomHeader("X-Authorization-Token", token)) {
          completeWith("Ok")
        }
      } else {
        completeWith("Fail")
      }
    }
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

  def ifAuthentificated(f: RequestContext ⇒ Unit): RequestContext ⇒ Unit = { ctx ⇒

    if (isAuthentificationOk(ctx))
      completeWithContext(ctx) {
        f
      }
    else
      completeWithContext(ctx) {
        parameter("token".as[Long]?) { token ⇒
          if (ctx.request.queryParams.size > 1 && token != None) {
            f
          } else {
            try {
              val a = ctx.request.query.toLong
              f
            } catch {
              case e: Throwable ⇒ reject()
            }
          }
        }
      }

  }

  def auth(f: RequestContext ⇒ Unit) = {
    ifAuthentificated(f) ~ authentification
  }

}