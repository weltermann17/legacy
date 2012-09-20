package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.Raw

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.ArrayColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.CompressedColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.UniqueColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.BooleanColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.FunctionColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTableFiller
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Filler
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository

class HiddenInstancesFiller(implicit connectionfactory: ConnectionFactory) extends InMemoryTableFiller[HiddenInstances] {

  val sql = """select /*+ all_rows */
(select count(*) from enovia.maniteminstance where vshownoshow = 'F'),
path
from ( 
   select vparentii as child, oid as parent, sys_connect_by_path(nvl(var, oid), '-') as path, connect_by_isleaf leaf
   from enovia.maniteminstance i
   start with oid in ( select oid from enovia.maniteminstance where vshownoshow = 'F' )
   connect by prior vparentii = oid ) 
where 1 = leaf
"""

}

class HiddenInstances(implicit @transient connectionfactory: ConnectionFactory, length: Int) extends InMemoryTable {

  type K = String

  type F = HiddenInstancesFiller

  val id = new UniqueColumn[String]

  def set(index: Int, row: RichResultSet) = {
    def convert(s: String): String = {
      val r = new StringBuilder
      var b = 0
      var e = 0
      while (-1 < { b = s.indexOf("-", e); b }) {
        e = s.indexOf("-", b + 1) match { case -1 => s.length case e => e }
        r.append(assemblyrelations(Raw(s.substring(b + 1, e)))).append("-")
      }
      r.toString
    }

    id(index, convert(row))
  }

  val columns = Map(
    "id" -> id)

  private def assemblyrelations = Repository.next(classOf[AssemblyRelations])

}

