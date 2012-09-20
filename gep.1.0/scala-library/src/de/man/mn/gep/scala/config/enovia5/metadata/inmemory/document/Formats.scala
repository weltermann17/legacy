package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Timestamp
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.Raw

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.ArrayColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.CompressedColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.BitSetColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.FunctionColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.UniqueColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTableFiller
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.LinkTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository

class FormatsFiller(implicit @transient connectionfactory: ConnectionFactory) extends InMemoryTableFiller[Formats] {

  val frompart = """from 
enovia.mandocumentrevision v,
enovia.mandocumentiteration i, 
enovia.mandociterationformat f, 
enovia.mandocsecuredfile s,
enoread.allvaultdocuments d
where i.oid = v.vpreferrediteration and i.oid = f.viteration and f.oid = s.viterationformat
and d.oid = hextoraw(substr(utl_raw.cast_to_varchar2(s.vvaultdoc), 15))"""

  val sql = """select /*+ all_rows */
(select count(*) """ + frompart + """),
rawtohex(d.oid),
rawtohex(v.oid),
f.vformattype,
f.v505submimetype,
s.v514filesize,
substr(d.doclocation, instr(d.doclocation, '/secured')),
s.v514lastmodified,
lower(substr(utl_raw.cast_to_varchar2(s.vvaultdoc), 9, 5)) 
""" + frompart

}

class Formats(implicit @transient connectionfactory: ConnectionFactory, length: Int) extends InMemoryTable {

  type K = Raw

  type F = FormatsFiller

  val id = new UniqueColumn[Raw]
  val document = new CompressedColumn[Int]
  val mimetype = new BitSetColumn[String]
  val submimetype = new BitSetColumn[String]
  val filesize = new ArrayColumn[Long]
  val filepath = new ArrayColumn[String]
  val lastmodified = new ArrayColumn[java.sql.Timestamp]
  val vault = new BitSetColumn[String]

  def set(index: Int, row: RichResultSet) = {
    val documents = Repository.next(classOf[Documents])

    id(index, Raw(row))
    document(index, documents(Raw(row)))
    mimetype(index, row)
    submimetype(index, row)
    filesize(index, row)
    filepath(index, row)
    lastmodified(index, row)
    vault(index, row)
  }

  val columns = Map(
    "id" -> id,
    "document" -> document,
    "mimetype" -> mimetype,
    "submimetype" -> submimetype,
    "filesize" -> filesize,
    "filepath" -> filepath,
    "lastmodified" -> lastmodified,
    "vault" -> vault)

}
