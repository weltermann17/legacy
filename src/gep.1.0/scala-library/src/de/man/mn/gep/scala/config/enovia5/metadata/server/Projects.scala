package de.man.mn.gep.scala.config.enovia5.metadata.server

import java.io.PrintWriter

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.nstring2s
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper
import com.ibm.de.ebs.plm.scala.util.Io.nullstream

class Projects
  extends DatabaseRepresentation {

  override lazy val sql = """select  /*+ result_cache */  
  distinct 
  prj.vid
from
  enovia.rscbase prj,
  enovia.rsccontext ctx,
  enovia.rscbase pers,
  enovia.rscrelationr rsc1,
  enovia.rscrelationvcomponentsa rsc2
where
  prj.type = (select entityuuid from enovia.rdbentitymapping where entityname = 'RscProject')
and
  prj.oid = ctx.vref1
and
  ctx.oid = rsc1.vparent
and
  rsc1.oid = rsc2.oid
and
  rsc2.value = pers.oid
and
  upper(pers.vid) = upper(:1)
"""

  def this(parameters: Map[String, String])(implicit baseuri: String, connectionfactory: ConnectionFactory) = {
    this()
    init(baseuri, connectionfactory, parameters)
    write(nullstream)
  }

  def toList: List[String] = {
    list.toList
  }

  def toInList: String = {
    val buf = new StringBuilder
    var i = 0
    list.foreach { p =>
      if (0 < i) buf.append(",")
      buf.append("'" + p + "'")
      i += 1
    }
    if (0 == buf.length) {
      buf.append("''")
    }
    buf.toString
  }

  case class Project(name: NString) extends PropertiesMapper

  override def doWrite(writer: PrintWriter): Unit = {
    for (
      project <- statement << parameters("user") <<! (result =>
        Project(result))
    ) {
      list += project.name
    }
  }

  private val list = new collection.mutable.ListBuffer[String]

}

