package de.man.mn.gep.scala.config.enovia5.metadata.server

object Conversions {

  def matchLocation(location: String): String = location match {
    case "muc" => "M\u00fcnchen"
    case "nbg" => "N\u00fcrnberg"
    case "sty" => "Steyr"
    case "slz" => "Salzgitter"
    case "vie" => "Wien"
    case "pos" => "Posen"
    case "stw" => "Starachowice"
    case _ => null
  }

  def status(status: String) = status match {
    case "IN_WORK" => "In work"
    case "IN_RELEASE" => "In release"
    case "RELEASED" => "Released"
    case "In work" => "In work"
    case "In release" => "In release"
    case "Released" => "Released"
    case "nls_WIP" => "In work"
    case s => "Unmapped(" + s + ")"
  }

  def lockowner(lockstatus: Option[String], lockuser: Option[String]) = {
    lockstatus match { case Some(ls) if "Y" == ls => lockuser case _ => None }
  }

  def isassembly(assembly: Int) = {
    2 == assembly
  }

  def isstandard(standard: Option[String]) = {
    standard match { case Some(s) if "T" == s => true case _ => false }
  }

  def isproduct(product: Option[String]) = {
    product match { case Some(s) if "F" == s => false case _ => true }
  }

  def ishidden(value: Option[String]) = value match {
    case Some(value) => Some("F" == value) case None => None
  }

  def isproject2d(value: Option[String]) = ishidden(value)

  def origin(baseuri: String) = {
    def tparm(name: String) = {
      if (baseuri.contains(name)) {
        val b = baseuri.indexOf(name + "/") + (name + "/").length
        val e = baseuri.indexOf("/", b)
        baseuri.substring(b, e)
      } else {
        null
      }
    }
    (tparm("divisions") match {
      case "engine" => "Engine"
      case "truck" => "Truck & Bus"
      case _ => "No match"
    }) + ", " + (tparm("subsystems") match {
      case "enovia5" => "Enovia V5"
      case _ => "No match"
    })
  }
}