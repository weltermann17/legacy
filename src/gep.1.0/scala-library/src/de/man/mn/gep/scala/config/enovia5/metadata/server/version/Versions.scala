package de.man.mn.gep.scala.config.enovia5.metadata.server.version

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Date
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String

import de.man.mn.gep.scala.config.enovia5.metadata.server.ResultSetRepresentation

class Versions extends ResultSetRepresentation {

  override lazy val sql = """select /*+ result_cache */ /*+ first_rows(50) */ 
rnum, 
utl_raw.cast_to_raw(vid), 
utl_raw.cast_to_raw(vversion), 
utl_raw.cast_to_raw(mnecnum),
utl_raw.cast_to_raw(mndescde),
vstatus,
vuser,
utl_raw.cast_to_raw(vproject0020),
lockstatus,
lockuser, 
cmodified,
v511parttype,
mnpartstd,
rawtohex(masterid),
rawtohex(oid)
from (
select t.*, rownum rnum from (
select a.*, b.oid as masterid, b.vid, b.v511parttype, #ORDERBYALIAS# 
from enovia.manpartversion a, enovia.manpartmaster b 
where a.vmaster = b.oid and b.v514lastversion = a.oid and a.vproject0020 in (#PROJECTS#) and #QUERY#
order by #ORDERBY#) 
t where rownum <= :1) 
where rnum > :2 order by orderbyalias #ASCENDINGDECENDING# 
"""
    .replace("#QUERY#", query)
    .replace("#ORDERBY#", orderby)
    .replace("#ORDERBYALIAS#", orderby.replace(" asc", " as orderbyalias").replace(" desc", " as orderbyalias"))
    .replace("#ASCENDINGDECENDING#", if (orderby.contains("desc")) "desc" else "asc")

  override lazy val sqlcount = """
select count(*) 
from enovia.manpartversion a, enovia.manpartmaster b 
where a.vmaster = b.oid 
and b.v514lastversion = a.oid 
and a.vproject0020 in (#PROJECTS#) 
and #QUERY#
"""
    .replace("#QUERY#", query)

  class Row(result: RichResultSet) extends VersionDetail(
    row = result,
    name = result,
    versionstring = result,
    changerequest = result,
    description_de = result,
    description_en = None,
    description_fr = None,
    description_pl = None,
    description_tr = None,
    material = None,
    weight = None,
    statusstring = result,
    owner = result,
    team = None,
    project = result,
    lockstatus = result,
    lockuser = result,
    creator = None,
    lastmodified = result,
    created = None,
    assembly = result,
    standardpart = result,
    masterid = result,
    id = result,
    quantity = None,
    level = None)

  override protected def prepare = statement << to << from

  override protected def row(result: RichResultSet) = new Row(result)

}
