package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.partner

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers._
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Timestamp
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.Raw
import de.man.mn.gep.scala.config.enovia5.metadata.server.Conversions

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory._

class PartnersFiller(implicit connectionfactory: ConnectionFactory) extends InMemoryTableFiller[Partners] {

  val sql = """select /*+ all_rows */
(select count(*) from enoread.manpartner),
rawtohex(oid),
utl_raw.cast_to_raw(vid), 
utl_raw.cast_to_raw(vname),
utl_raw.cast_to_raw(vdescription),
vuser,
vorganization,
vproject,
lockstatus,
lockuser,
cmodified,
ccreated
from enoread.manpartner
"""

}

class Partners(implicit @transient connectionfactory: ConnectionFactory, length: Int) extends InMemoryTable {

  type K = Raw

  type F = PartnersFiller

  val id = new UniqueColumn[Raw]
  val name = new UniqueColumn[NString]
  val alternatename = new MostlyNullColumn[NString]
  val description = new MostlyNullColumn[NString]
  val owner = new CompressedColumn[String]
  val team = new CompressedColumn[String]
  val project = new BitSetColumn[String]
  val lockowner = new CompressedColumn[Option[String]]
  val lastmodified = new ArrayColumn[java.sql.Timestamp]
  val created = new ArrayColumn[java.sql.Timestamp]
  val mappings = new FunctionColumnA[Set[Int], String] {
    val f = (index: Int) => (mapping: String) => {
      partnermappings.partner.lookup(id(index)) & partnermappings.mapping.lookup(mapping)
    }
  }

  def set(index: Int, row: RichResultSet) = {
    id(index, Raw(row))
    name(index, row)
    alternatename(index, row)
    description(index, row)
    owner(index, row)
    team(index, row)
    project(index, row)
    lockowner(index, Conversions.lockowner(row, row))
    lastmodified(index, row)
    created(index, row)
  }

  val columns = Map(
    "id" -> id,
    "name" -> name,
    "alternatename" -> alternatename,
    "description" -> description,
    "owner" -> owner,
    "team" -> team,
    "project" -> project,
    "lockowner" -> lockowner,
    "lastmodified" -> lastmodified,
    "created" -> created)

  private def partnermappings = Repository(classOf[PartnerMappings])

}

class PartnerMappingsFiller(implicit connectionfactory: ConnectionFactory) extends InMemoryTableFiller[PartnerMappings] {

  val sql = """select /*+ all_rows */
(select count(*) from enoread.manpartnermapping),
rawtohex(vpartner),
vmapping, 
vaction,
vfromtable,
vfromcolumn,
vattribute,
vproperty,
utl_raw.cast_to_raw(vdescription)
from enoread.manpartnermapping
"""

}

class PartnerMappings(implicit @transient connectionfactory: ConnectionFactory, length: Int) extends InMemoryTable {

  type K = Null

  type F = PartnerMappingsFiller

  val id: Null = null
  val partner = new BitSetColumn[Raw]
  val mapping = new BitSetColumn[String]
  val action = new BitSetColumn[Option[String]]
  val fromtable = new CompressedColumn[Option[String]]
  val fromcolumn = new CompressedColumn[Option[String]]
  val attribute = new CompressedColumn[Option[String]]
  val property = new CompressedColumn[Option[String]]
  val description = new MostlyNullColumn[String]

  def set(index: Int, row: RichResultSet) = {
    partner(index, Raw(row))
    mapping(index, row)
    action(index, row)
    fromtable(index, row)
    fromcolumn(index, row)
    attribute(index, row)
    property(index, row)
    description(index, row)
  }

  val columns = Map(
    "partner" -> partner,
    "mapping" -> mapping,
    "action" -> action,
    "fromtable" -> fromtable,
    "fromcolumn" -> fromcolumn,
    "attribute" -> attribute,
    "property" -> property,
    "description" -> description)

}

