package de.man.mn.gep.scala.config.enovia5.metadata.server.shared

import java.io.PrintWriter

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.ps2Rich
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper

import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

class VaultDocument extends DatabaseRepresentation {

  override lazy val sql = """select /*+ result_cache */ /*+ first_rows(1) */
doclocation
from 
#VAULT#.vaultdocument
where oid = hextoraw(:1) 
"""
    .replace("#VAULT#", parameters("vault"))

  def this(parameters: Map[String, String])(implicit baseuri: String, connectionfactory: ConnectionFactory) = {
    this()
    init(baseuri, connectionfactory, parameters)
  }

  case class Document(
    doclocation: Option[String])
    extends PropertiesMapper {

    override def toString = doclocation match { case Some(s) => s case None => null }
  }

  override def doWrite(writer: PrintWriter) = {
    for (
      document <- statement << parameters("oid") <<! (result =>
        Document(result))
    ) {
      writer.print(document)
    }
  }
}
