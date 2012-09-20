package com.ibm.de.ebs.plm.scala.rest

import org.restlet.data.ChallengeScheme
import org.restlet.data._
import org.restlet.routing.Filter.CONTINUE
import org.restlet.security.ChallengeAuthenticator
import org.restlet.security.LocalVerifier
import org.restlet.security.Verifier
import org.restlet.Context
import org.restlet.Request
import org.restlet.Response
import org.restlet.Restlet

import com.ibm.de.ebs.plm.scala.rest.HeaderUtils.getCustomHeader
import com.ibm.de.ebs.plm.scala.rest.HeaderUtils.renameHeader
import com.ibm.de.ebs.plm.scala.rest.HeaderUtils.setCustomHeader
import com.ibm.de.ebs.plm.scala.security.MessageDigest.MD5
import com.ibm.de.ebs.plm.scala.text.Uuid.newUuid

case class BasicAuthenticator(next: Restlet, verifier: Verifier, context: Context)
  extends ChallengeAuthenticator(context, false, ChallengeScheme.HTTP_BASIC, "GEPserver", verifier)
  with WhiteList {

  setNext(next)

  override protected def preProcess(request: Request, response: Response) = {
    val hasauthorization = renameHeader(request, "X-Authorization", "Authorization")
    if (hasauthorization) {
      if (Method.HEAD == request.getMethod) {
        val token = newUuid.toString
        requesttokens.put(hash(request), token)
        authorizationtokens.put(token, getCustomHeader(request, "Authorization"))
        setCustomHeader(response, "X-Authorization-Token", token)
        tokenmediatypes.put(token, Services.getPreferredMediaType(request))
      }
    } else {
      val token = {
        val query = request.getResourceRef.getQuery
        if (null != query) {
          val b = query.indexOf("token=")
          if (0 < b) {
            val e = query.indexOf("&", b)
            query.substring(b + "token=".length, if (-1 == e) query.length else e)
          } else query
        } else null
      }
      if (null != token) {
        val key = hash(request)
        val storedtoken = requesttokens.remove(key)
        if (token == storedtoken) {
          setCustomHeader(request, "Authorization", authorizationtokens.remove(token))
          Services.setPreferredMediaType(request, tokenmediatypes.remove(token))
        }
      }
    }
  }

  override protected def postProcess(request: Request, response: Response) = {
    val key = hash(request)
    if (Method.HEAD == request.getMethod && requesttokens.containsKey(key)) {
      val token = requesttokens.get(key)
      setCustomHeader(response, "X-Authorization-Token", token)
    }
  }

}

case class DigestAuthenticator(next: Restlet, verifier: LocalVerifier, context: Context)
  extends org.restlet.ext.crypto.DigestAuthenticator(context, "GEPserver", "serverkey")
  with WhiteList {

  setNext(next)
  setWrappedVerifier(verifier)

}

trait WhiteListEntry { def allow(request: Request): Boolean }

trait WhiteList extends ChallengeAuthenticator {

  setRechallenging(true)

  abstract override def beforeHandle(request: Request, response: Response): Int = {
    preProcess(request, response)
    if (whitelist.exists(_.allow(request))) {
      CONTINUE
    } else {
      super.beforeHandle(request, response)
    }
  }

  abstract override def afterHandle(request: Request, response: Response): Unit = {
    super.afterHandle(request, response)
    postProcess(request, response)
  }

  protected def preProcess(request: Request, response: Response) = {}

  protected def postProcess(request: Request, response: Response) = {}

  def ++(e: WhiteListEntry) = {
    whitelist = e :: whitelist
    this
  }

  def ++(s: String) = {
    whitelist = PathContains(s) :: whitelist
    this
  }

  protected def hash(request: Request) = {
    MD5(request.getResourceRef.getPath + request.getClientInfo.getAddress)
  }

  private case class PathContains(s: String) extends WhiteListEntry {

    def allow(request: Request) = {
      request.getResourceRef.getPath.contains(s)
    }

  }

  protected val authorizationtokens = new java.util.concurrent.ConcurrentHashMap[String, String]
  protected val tokenmediatypes = new java.util.concurrent.ConcurrentHashMap[String, MediaType]
  protected val requesttokens = new java.util.concurrent.ConcurrentHashMap[String, String]

  private var whitelist = List[WhiteListEntry]()

}

