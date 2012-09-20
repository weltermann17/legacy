package de.man.mn.gep.scala.config.enovia5.metadata.server.product

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Date
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String

import de.man.mn.gep.scala.config.enovia5.metadata.server.ResultSetRepresentation

class Products extends ResultSetRepresentation {

  override lazy val sql = """select /*+ result_cache */ /*+ first_rows(50) */ 
rnum, 
utl_raw.cast_to_raw(vid),
utl_raw.cast_to_raw(vname||' '||vdescription),
vuser,
utl_raw.cast_to_raw(vproject0004),
lockstatus,
lockuser,
cmodified,
rawtohex(oid)
from (
select t.*, rownum rnum from (
select a.*, #ORDERBYALIAS# 
from enovia.manproductrootclass a
where #QUERY#
and a.vproject0004 in (#PROJECTS#)
order by #ORDERBY#) 
t where rownum <= :1) 
where rnum > :2 order by orderbyalias #ASCENDINGDECENDING# 
"""
    .replace("#QUERY#", query)
    .replace("#ORDERBY#", orderby)
    .replace("#ORDERBYALIAS#", orderby.replace(" asc", " as orderbyalias").replace(" desc", " as orderbyalias"))
    .replace("#ASCENDINGDECENDING#", if (orderby.contains("desc")) "desc" else "asc")

  override lazy val sqlcount = """select /*+ result_cache */ 
count(*) 
from enovia.manproductrootclass a
where #QUERY#
and a.vproject0004 in (#PROJECTS#) 
"""
    .replace("#QUERY#", query)

  class Row(result: RichResultSet) extends ProductDetail(
    row = result,
    name = result,
    description_de = result,
    owner = result,
    team = None,
    project = result,
    lockstatus = result,
    lockuser = result,
    lastmodified = result,
    created = None,
    id = result,
    instances = None,
    parts = None,
    level = None)

  override protected def prepare = statement << to << from

  override protected def row(result: RichResultSet) = new Row(result)

}