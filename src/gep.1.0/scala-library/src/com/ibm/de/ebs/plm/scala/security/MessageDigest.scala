package com.ibm.de.ebs.plm.scala.security

object MessageDigest {

  object MD5 {
    def apply(bytes: Array[Byte]): String = {
      val md5 = java.security.MessageDigest.getInstance("MD5")
      md5.reset()
      md5.update(bytes)
      md5.digest().map(0xff & _).map { "%02x".format(_) }.foldLeft("") { _ + _ }
    }
    def apply(s: String): String = apply(s.getBytes)
  }

}