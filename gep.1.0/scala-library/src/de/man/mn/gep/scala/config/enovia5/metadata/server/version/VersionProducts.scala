package de.man.mn.gep.scala.config.enovia5.metadata.server.product

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Date
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String

import de.man.mn.gep.scala.config.enovia5.metadata.server.ResultSetRepresentation

class VersionProducts extends ResultSetRepresentation {

  override lazy val sql = """select /*+ result_cache */ /*+ first_rows(50) */ 
rnum, 
utl_raw.cast_to_raw(vid) as vid,
utl_raw.cast_to_raw(vname||' '||vdescription),
vuser,
lockstatus,
lockuser,
cmodified,
rawtohex(oid)
from (
select t.*, rownum rnum from (
select a.*
from enovia.manproductrootclass a,
enovia.maniteminstance b,
enovia.manpartversion c
where a.oid = b.vparentprc
and c.oid = b.vpv
and c.oid = hextoraw(:1)
and a.vproject0004 in (#PROJECTS#)
order by a.vid) 
t where rownum <= :2) 
where rnum > :3 order by vid 
"""

  override lazy val sqlcount = """select /*+ result_cache */ 
count(*) 
from enovia.manproductrootclass a
from enovia.manproductrootclass a,
enovia.maniteminstance b,
enovia.manpartversion c
where a.oid = b.vparentprc
and c.oid = b.vpv
and c.oid = hextoraw(:1)
and a.vproject0004 in (#PROJECTS#) 
"""

  class Row(result: RichResultSet) extends ProductDetail(
    row = result,
    name = result,
    description_de = result,
    owner = result,
    team = None,
    project = None,
    lockstatus = result,
    lockuser = result,
    lastmodified = result,
    created = None,
    id = result,
    instances = None,
    parts = None,
    level = None)

  override protected def prepare = statement << parameters("version") << to << from

  override protected def row(result: RichResultSet) = new Row(result)

}