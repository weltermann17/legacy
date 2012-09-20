package com.ibm.de.ebs.plm.scala.util

object Environment {

  def getEnvOrElse(name: String, notfound: String = null)(implicit f: String => String = s => s) = {

    System.getenv(name) match {
      case null => if (null == notfound) "<" + name + ">" else notfound
      case s => f(s)
    }

  }

}