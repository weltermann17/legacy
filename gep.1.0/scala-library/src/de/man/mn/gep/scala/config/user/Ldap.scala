package de.man.mn.gep.scala.config.user

import com.ibm.de.ebs.plm.scala.caching._
import com.ibm.de.ebs.plm.scala.rest._
import com.ibm.de.ebs.plm.scala.naming.directory._
import com.ibm.de.ebs.plm.scala.json._
import com.ibm.de.ebs.plm.scala.naming.directory.LdapQuery
import com.ibm.de.ebs.plm.scala.text.StringConversions._
import com.ibm.de.ebs.plm.scala.naming.directory.LdapQuery

object Ldap {
  def User(attributes: Map[String, String]): String = {
    val query = "(manUserkennung=" + attributes("user") + ")"
    val base = List(
      "ldap://mmx500-1.mn-man.biz:389/o=MAN%20Nutzfahrzeuge,cn=MAN%20Konzern",
      "ldap://mmx500-2.mn-man.biz:389/o=MAN%20Nutzfahrzeuge,cn=MAN%20Konzern")
    var managerfirstname = ""
    var managerlastname = ""
    def managerquery: Option[String => String] = Some({ v: String =>
      val mapping: List[Tuple3[String, String, Option[String => String]]] = List(
        ("manUserkennung", "id", Some({ s => s.toUpperCase })),
        ("manPriSurname", "lastname", None),
        ("manPriGivenname", "firstname", None))
      val manager = { val b = v.indexOf("cn="); val e = v.indexOf(",", b); v.substring(b, e) }
      val query = LdapQuery(base, Some("(" + manager + ")"), None, None, mapping)
      query("id") match {
        case Some(m) =>
          managerfirstname = query("firstname") match { case Some(mf) => mf case None => "" }
          managerlastname = query("lastname") match { case Some(mf) => mf case None => "" }
          m
        case None => ""
      }
    })
    val mapping: List[Tuple3[String, String, Option[String => String]]] = List(
      ("manUserkennung", "id", Some({ s => s.toUpperCase })),
      ("manPriSurname", "lastname", None),
      ("manPriGivenname", "firstname", None),
      ("mail", "mailto", None),
      ("telephonenumber", "phone", None),
      ("mannotesName", "company", None),
      ("manAbteilung", "department", None),
      ("postalCode", "zipcode", None),
      ("manPostalCity", "city", None),
      ("street", "street", Some({ s => s.replace("trasse", "tra\u00dfe") })),
      ("postofficebox", "pobox", None),
      ("manPlzPostOfficeBox", "pozipcode", None),
      ("manGebaeude", "building", None),
      ("manRaum", "room", None),
      ("manVorgesetzterDisziplinarisch", "manager", managerquery),
      ("manVorgesetzterDisziplinarischWF", "manager", managerquery),
      ("manVorgesetzterFachlichWF", "manager", managerquery),
      ("manPriSurname", "managerfirstname", Some({ _ => managerfirstname })),
      ("manPriGivenname", "managerlastname", Some({ _ => managerlastname })))
    LdapQuery(base, Some(query), None, None, mapping).toString
  }

  def Password(attributes: Map[String, String]): String = {
    val base = List(
      "ldap://mndemucdc01.mn-man.biz:389/dc=mn-man,dc=biz",
      "ldap://mndenbgdc01.mn-man.biz:389/dc=mn-man,dc=biz",
      "ldap://mndemucdc02.mn-man.biz:389/dc=mn-man,dc=biz",
      "ldap://mndenbgdc02.mn-man.biz:389/dc=mn-man,dc=biz",
      "ldap://mndemucdc03.mn-man.biz:389/dc=mn-man,dc=biz",
      "ldap://mndenbgdc03.mn-man.biz:389/dc=mn-man,dc=biz",
      "ldap://mndemucdc04.mn-man.biz:389/dc=mn-man,dc=biz",
      "ldap://mndenbgdc04.mn-man.biz:389/dc=mn-man,dc=biz",
      "ldap://mndemucdc05.mn-man.biz:389/dc=mn-man,dc=biz",
      "ldap://mndemucdc06.mn-man.biz:389/dc=mn-man,dc=biz")
    val user = Some(attributes("user") + "@mn-man.biz")
    val password = Some(fromCryptHexString(attributes("password")))
    LdapQuery(base, None, user, password, Nil).toString
  }

}