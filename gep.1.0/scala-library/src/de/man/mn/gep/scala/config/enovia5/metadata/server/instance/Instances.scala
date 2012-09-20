package de.man.mn.gep.scala.config.enovia5.metadata.server.instance

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichPreparedStatement
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String

import de.man.mn.gep.scala.config.enovia5.metadata.server.ResultSetRepresentation

class Instances extends ResultSetRepresentation {

  parent =>

  override lazy val sql = """select /*+ result_cache */ /*+ first_rows(50) */ 
rnum, 
utl_raw.cast_to_raw(vinstanceid),
utl_raw.cast_to_raw(vid), 
v511parttype,
utl_raw.cast_to_raw(vversion), 
rawtohex(vpv),
utl_raw.cast_to_raw(productid), 
rawtohex(productoid),
utl_raw.cast_to_raw(parentid), 
utl_raw.cast_to_raw(parentversion), 
utl_raw.cast_to_raw(parentmndescde),
rawtohex(parentoid),
vlevel,
vshownoshow,
vproject2d,
lockstatus,
lockuser, 
rawtohex(oid)
from (
select t.*, rownum rnum from (
select 
i.*, 
m.vid,
m.v511parttype,
v.vversion,
prc.vid as productid,
prc.oid as productoid,
mv.parentid,
mv.parentversion,
mv.parentmndescde,
mv.parentoid
from 
enovia.maniteminstance i left outer join ( select
ii.oid as oid,
m.vid as parentid, 
v.vversion as parentversion,
v.mndescde as parentmndescde,
v.oid as parentoid
from enovia.manpartversion v, 
enovia.manpartmaster m, 
enovia.maniteminstance ii
where v.vmaster = m.oid 
and m.v514lastversion = v.oid
and ii.vpv = v.oid ) mv
on i.vparentii = mv.oid,
enovia.manproductrootclass prc,
enovia.manpartversion v,
enovia.manpartmaster m
where i.vpv = hextoraw(:1) 
and i.vparentprc = prc.oid
and v.oid = i.vpv
and m.oid = v.vmaster
order by upper(prc.vid), parentid, i.vinstanceid ) 
t where rownum <= :2 ) 
where rnum > :3  
"""

  override lazy val sqlcount = """select /*+ result_cache */ count(*) from enovia.maniteminstance where vpv = hextoraw(:1)
"""

  lazy val version = parameters("version")

  class Row(result: RichResultSet) extends InstanceDetail(
    row = result,
    instance = result,
    name = result,
    assembly = result,
    versionstring = result,
    versionoid = result,
    product = result,
    productoid = result,
    parentname = result,
    parentversion = result,
    description_de = result,
    parentoid = result,
    level = result,
    shownoshow = result,
    shownoshowparent = None,
    project2d = result,
    lockstatus = result,
    lockuser = result,
    id = result)

  override protected def prepare = statement << version << to << from

  override protected def preparecount(implicit statement: RichPreparedStatement) = statement << version

  override protected def row(result: RichResultSet) = new Row(result)

}
