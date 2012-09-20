package com.ibm.de.ebs.plm.scala.text

class Uuid {

  override def toString = uuid.toString.replace("-", "")

  private var uuid: java.util.UUID = null

}

object Uuid {

  def newUuid: Long = org.codehaus.aspectwerkz.proxy.Uuid.newUuid

  def newType4Uuid = {
    val uuid = new Uuid
    uuid.uuid = java.util.UUID.randomUUID
    uuid
  }

  implicit def uuid2string(uuid: Uuid): String = uuid.toString

}
