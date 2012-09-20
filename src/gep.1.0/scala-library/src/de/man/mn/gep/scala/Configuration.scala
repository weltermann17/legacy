package de.man.mn.gep.scala

import de.man.mn.gep.scala.config.enovia5.catia5.Derive3dxml
import de.man.mn.gep.scala.config.enovia5.catia5.DeriveCATProduct
import de.man.mn.gep.scala.config.enovia5.catia5.DerivePlmxml

case class Configuration(parameters: Map[String, String], authorization: org.restlet.data.ChallengeResponse) {

  def remoteHost(division: String, location: String) = {
    Configuration.config.getOrElse((division, location, "local"), (null, null))._1
  }

  def remoteHost(location: String): String = {
    remoteHost(parameters("division"), location)
  }

  def isRemote = {
    Configuration.thislocation != parameters("vault")
  }

  def derive = {
    parameters("derivedformat") match {
      case "3dxml" => new Derive3dxml(parameters, authorization)
      case "plmxml" => new DerivePlmxml(parameters, authorization)
      case "catproduct" => new DeriveCATProduct(parameters, authorization)
      case invalid => throw new Exception("Invalid derivedformat : " + invalid)
    }
  }

  def hostUrl = {
    val vaulttype = if (parameters("division") == Configuration.thisdivision) if (parameters("vault") == Configuration.thislocation) "local" else "cache" else "local"
    Configuration.config.getOrElse((parameters("division"), Configuration.thislocation, vaulttype),
      Configuration.config.getOrElse((Configuration.thisdivision, Configuration.thislocation, vaulttype), (null, null)))
  }

  def sameDivision = {
    parameters("division") == Configuration.thisdivision
  }

  def is3dxmlPart = {
    parameters.contains("nativeformat") && parameters("nativeformat") == "3dxml"
  }

  override def toString = parameters + Configuration.toString
}

object Configuration {

  lazy val applicationHost = config.getOrElse((thisdivision, thislocation, "local"), (null, null))._1

  private lazy val thislocation = Server.get("de.man.mn.gep.enovia5.vault.this.location").asString
  private lazy val thisdivision = Server.get("de.man.mn.gep.enovia5.vault.this.division").asString
  private lazy val thistype = Server.get("de.man.mn.gep.enovia5.vault.this.type").asString
  private lazy val config = {
    val vaultconfig = Server.get("de.man.mn.gep.enovia5.vault.configuration").asArray
    vaultconfig.foldLeft(Map[(String, String, String), (String, String)]()) {
      case (m, e) => val l = e.asArray; m ++ Map((l(0).asString, l(1).asString, l(2).asString) -> (l(3).asString, l(4).asString))
    }
  }

  def getConfig = config
  def thisDivision = thisdivision
  def thisLocation = thislocation
  def thisType = thistype

  override def toString = config.toString

}