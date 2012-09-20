package de.man.mn.gep.scala.config.enovia5.metadata.server

import org.squeryl.adapters.OracleAdapter

import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.SquerylHelpers
import com.ibm.de.ebs.plm.scala.util.Timers.Int2Value

import de.man.mn.gep.scala.Server

class Enovia5ConnectionFactory extends ConnectionFactory {

  val driver = Server.get("de.man.mn.gep.database.driver").asString
  val url = Server.get("de.man.mn.gep.database.connection.url").asString
  override val poolmin = Server.get("de.man.mn.gep.database.connection.pool.min").asInt
  override val poolmax = Server.get("de.man.mn.gep.database.connection.pool.max").asInt

  override def init = {
    try {
      System.setProperty("oracle.net.tns_admin", System.getenv("TNS_ADMIN"))
    } catch { case _ => }
    implicit val scheduler = new org.restlet.Application().getTaskService
    val connection = newConnection()
    SquerylHelpers.initialize(this, new OracleAdapter)
    connection.close
    this
  }

}