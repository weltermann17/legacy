package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.Raw

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.Versions
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.ArrayColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.CompressedColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.FunctionColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.UniqueColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTableFiller
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.LinkTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository

class DocumentsFiller(implicit @transient connectionfactory: ConnectionFactory) extends InMemoryTableFiller[Documents] {

  val sql = """select /*+ all_rows */
(select count(*) from enovia.mantpdocumentmaster m, enovia.mandocumentrevision v where m.oid = v.vmaster),
rawtohex(v.oid),
rawtohex(v.vpreferrediteration),
utl_raw.cast_to_raw(m.vid),
utl_raw.cast_to_raw(v.vversion) 
from enovia.mantpdocumentmaster m, 
enovia.mandocumentrevision v
where m.oid = v.vmaster
"""

}

class Documents(implicit @transient connectionfactory: ConnectionFactory, length: Int) extends InMemoryTable {

  type K = Raw

  type F = DocumentsFiller

  val id = new UniqueColumn[Raw]
  val iteration = new UniqueColumn[Raw]
  val name = new ArrayColumn[NString]
  val versionstring = new CompressedColumn[NString]
  val formats = new FunctionColumn[Set[Int]] {
    val f = (index: Int) => formats_.document.lookup(index)
  }

  def set(index: Int, row: RichResultSet) = {
    id(index, Raw(row))
    iteration(index, Raw(row))
    name(index, row)
    versionstring(index, row)
  }

  val columns = Map(
    "id" -> id,
    "iteration" -> iteration,
    "name" -> name,
    "versionstring" -> versionstring)

  private def formats_ = Repository(classOf[Formats])

}

class VersionDocumentsFiller(implicit connectionfactory: ConnectionFactory) extends InMemoryTableFiller[VersionDocuments] {

  val sql = """select /*+ first_rows */
(select count(*) from enovia.vpmtprelationcfroma f, enovia.vpmtprelationctoa t where f.oid = t.oid and f.rank = t.rank and f."OID$" = hextoraw('80A2B3BD0000520C383BE4950005A029')),
rawtohex(utl_raw.substr(f.cfrom, 2, 16)),
rawtohex(utl_raw.substr(t.cto, 2, 16))
from enovia.vpmtprelationcfroma f, 
enovia.vpmtprelationctoa t 
where f.oid = t.oid and f.rank = t.rank and f."OID$" = hextoraw('80A2B3BD0000520C383BE4950005A029')
"""

}

class VersionDocuments(implicit @transient val connectionfactory: ConnectionFactory, length: Int) extends LinkTable {

  type F = VersionDocumentsFiller

  type Link = Nothing

  type From = Versions

  type To = Documents

  override def set(index: Int, row: RichResultSet) = {
    val versions = Repository.next(classOf[Versions])
    val documents = Repository.next(classOf[Documents])

    id(index, index)
    val v = versions(Raw(row))
    val d = documents(Raw(row))
    val valid = -1 < v && -1 < d
    from(index, if (valid) v else -1)
    to(index, if (valid) d else -1)
    project(index, if (valid) versions.project(v) else "")
  }

}

