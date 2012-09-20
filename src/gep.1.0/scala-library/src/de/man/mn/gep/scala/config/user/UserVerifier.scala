package de.man.mn.gep.scala.config.user

import org.restlet.data.MediaType
import org.restlet.security.LocalVerifier
import org.restlet.security.Verifier

import com.ibm.de.ebs.plm.scala.json.JsonConversions.Any2Json
import com.ibm.de.ebs.plm.scala.json.JsonConversions.Json2Object
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.security.Crypt
import com.ibm.de.ebs.plm.scala.text.StringConversions._

import de.man.mn.gep.scala.Server

object UserVerifier extends LocalVerifier {

  override def getLocalSecret(identifier: String) = {
    val key = identifier.toLowerCase
    if (secrets.containsKey(key)) {
      secrets.get(key).toCharArray
    } else {
      if (addAuthentication(identifier)) {
        secrets.get(key).toCharArray
      } else {
        null
      }
    }
  }

  override def verify(identifier: String, secret: Array[Char]) = {
    val ssecret = new String(secret)
    addCredentials(identifier, ssecret)
    val larray = getLocalSecret(identifier)
    if (null != larray) {
      val lsecret = new String(larray)
      val cryptsecret = Crypt.crypt(lsecret, ssecret)
      if (cryptsecret == lsecret) {
        Verifier.RESULT_VALID
      } else {
        Verifier.RESULT_INVALID
      }
    } else {
      Verifier.RESULT_MISSING
    }
  }

  def addCredentials(principal: String, credentials: String) = {
    plaintext.put(principal.toLowerCase, toCryptHexString(credentials))
  }

  private def addAuthentication(identifier: String) = {
    try {
      val credentials = plaintext.get(identifier.toLowerCase)
      val json = Server.applicationResource("/users/signon/" + identifier + "/" + credentials + "/", null).get(MediaType.APPLICATION_JSON)
      val secret = {
        val result = Json.parse(json.getText).get("response").asObject.get("data").asArray(0).asObject
        result.get("password").asString.replace("{CRYPT}", "")
      }
      secrets.put(identifier.toLowerCase, secret)
      true
    } catch {
      case e => println("addAuthentication(" + identifier + ") failed : " + e); false
    }
  }

  private lazy val secrets = new java.util.concurrent.ConcurrentHashMap[String, String]
  private lazy val plaintext = new java.util.concurrent.ConcurrentHashMap[String, String]

}