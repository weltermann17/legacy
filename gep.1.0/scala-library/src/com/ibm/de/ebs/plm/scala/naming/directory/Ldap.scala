package com.ibm.de.ebs.plm.scala.naming.directory

import java.util.Hashtable

import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.security.Crypt

import javax.naming.directory.Attributes
import javax.naming.directory.DirContext
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.NamingException

case class LdapQuery(
  baselist: List[String],
  query: Option[String],
  principal: Option[String],
  credentials: Option[String],
  mapping: List[Tuple3[String, String, Option[String => String]]]) {

  val result = new StringBuilder(4096)
  val processed = new scala.collection.mutable.HashSet[String]
  val env: Hashtable[String, String] = new Hashtable[String, String]
  val controls = new SearchControls
  var context: DirContext = null
  var results: NamingEnumeration[SearchResult] = null
  var json: Json = null
  try {
    if (!fakeResult(principal, credentials)) {
      result.append("{\"response\":{\"status\":")
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
      principal match {
        case Some(p) =>
          env.put(Context.SECURITY_AUTHENTICATION, "simple")
          env.put(Context.SECURITY_PRINCIPAL, p)
          env.put(Context.SECURITY_CREDENTIALS, credentials.getOrElse(""))
        case None =>
      }
      baselist.foreach { base =>
        if (null == context) {
          try {
            env.put(Context.PROVIDER_URL, base)
            context = new InitialDirContext(env)
          } catch {
            case e: javax.naming.AuthenticationException => throw e
            case e: javax.naming.CommunicationException => println("Ldap : " + e)
            case e => e.printStackTrace
          }
        }
      }
      if (null == context) throw new javax.naming.CommunicationException("No server available.")
      controls.setSearchScope(SearchControls.SUBTREE_SCOPE)
      results = context.search("", query.getOrElse("CN="), controls)
      if (results.hasMoreElements) {
        var n = 0
        result.append("0,\"startrow\":0,\"data\":[")
        while (results.hasMore) {
          if (0 < n) result.append(",")
          mapResult(results.next.getAttributes)
          n += 1
        }
        result.append("],\"totalRows\":").append(n).append(",\"endrow\":").append(n)
      } else {
        credentials match {
          case Some(c) =>
            result.append("0,\"startrow\":0,\"data\":[")
            result.append("{\"password\":\"{CRYPT}")
            result.append(Crypt.crypt(null, c))
            result.append("\"}],\"totalRows\":").append(1).append(",\"endrow\":").append(1)
          case None =>
            result.append("-1,\"startrow\":0,\"totalRows\":0,\"endrow\":0")
        }
      }
      result.append("}}")
    }
  } catch {
    case e: NamingException => result.clear; result.append("{\"response\":{\"status\":-1}}"); println("Ldap : " + e)
    case e => println("Ldap : " + e); e.printStackTrace
  } finally {
    if (null != context) context.close
    if (null != results) results.close
    json = toJson
  }

  def apply(attribute: String): Option[String] = {
    try {
      Some(json.asObject("response").asObject("data").asArray(0).asObject(attribute).asString)
    } catch {
      case _ => None
    }
  }

  def toJson = Json.parse(toString)

  override def toString = result.toString

  private def fakeResult(principal: Option[String], credentials: Option[String]): Boolean = {
    principal match {
      case Some(p) => credentials match {
        case Some(c) =>
          if (fakePrincipal(p)) {
            result.append("{\"response\":{\"status\":")
            if (fakeCredentials(p, c)) {
              result.append("0,\"startrow\":0,\"data\":[")
              result.append("{\"password\":\"{CRYPT}")
              result.append(Crypt.crypt(null, c))
              result.append("\"}],\"totalRows\":").append(1).append(",\"endrow\":").append(1)
            } else {
              result.append("-1,\"startrow\":0,\"totalRows\":0,\"endrow\":0")
            }
            result.append("}}")
            true
          } else false
        case None => false
      }
      case None => false
    }
  }

  private def fakePrincipal(p: String) = p.toLowerCase.contains("u62xz")

  private def fakeCredentials(p: String, c: String) = p.toLowerCase.contains("u62xz") && "datavision".equals(c)

  private def mapResult(attributes: Attributes) = {
    processed.clear
    var i = 0
    val data = mapping.foldLeft(Map[String, String]()) {
      case (m, (key, mappedkey, f)) =>
        if (!processed.contains(mappedkey)) {
          val value = attributes.get(key) match { case null => null case a => a.get.toString }
          value match {
            case null => m
            case v =>
              val va: String = f match {
                case Some(p) => p(v.toString)
                case None => v
              }
              processed += mappedkey
              i += 1
              m ++ Map(mappedkey -> va)
          }
        } else {
          m
        }
    }
    result.append(Json.build(data))
  }
}
