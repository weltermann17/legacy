package de.man.mn.gep.scala.config.enovia5.metadata.server.version

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichPreparedStatement
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper

import de.man.mn.gep.scala.config.enovia5.metadata.server.ResultSetRepresentation

class Bom extends ResultSetRepresentation {

  parent =>

  override lazy val sql = """select r.*, rownum as rfrom from (
select
rnum,
clevel,
vid, 
vversion, 
mnecnum,
mndescde,
vstatus,
vuser,
lockstatus,
lockuser, 
cmodified,
v511parttype,
mnpartstd,
masterid,
oid,
mpath,
rownum as rfrom
from (
select /*+ result_cache */ 
count(*) as rnum,
clevel,
vid, 
vversion, 
mnecnum,
mndescde,
vstatus,
vuser,
lockstatus,
lockuser, 
cmodified,
v511parttype,
mnpartstd,
rawtohex(masterid) as masterid,
rawtohex(oid) as oid,
min(path) as mpath
from (
select /*+ all_rows */ 
path,
#LEVEL#||clevel as clevel,
vid, 
vversion, 
mnecnum,
mndescde,
vstatus,
vuser,
lockstatus,
lockuser, 
cmodified,
v511parttype,
mnpartstd,
masterid,
oid
from ( ( 
select /*+ all_rows */ 
path,
clevel,
utl_raw.cast_to_raw(pm.vid) as vid,
utl_raw.cast_to_raw(pv.vversion) as vversion,
utl_raw.cast_to_raw(pv.mnecnum) as mnecnum,
utl_raw.cast_to_raw(pv.mndescde) as mndescde,
pv.vstatus as vstatus, 
pv.vuser as vuser,
pv.lockstatus as lockstatus,
pv.lockuser as lockuser,
pv.cmodified as cmodified,
pm.v511parttype as v511parttype,
pv.mnpartstd as mnpartstd,
pm.oid as masterid,
pv.oid as oid 
from ( select 
'!'||sys_connect_by_path(trim(to_char(rownum, '0xxxxx')), '!') as path,
level as clevel,
parent, 
child
from ( select 
ar.vari as #PARENT#, 
pm.v514lastversion as #CHILD#
from enovia.vpmassemblyrelation ar, 
enovia.manpartmaster pm
where pm.oid = ar.vpm )
start with parent = hextoraw(:1) 
connect by prior child = parent ),
enovia.manpartversion pv,
enovia.manpartmaster pm
where pv.oid = child
and pv.vmaster = pm.oid
and pm.v514lastversion = pv.oid
and pv.vproject0020 in (#PROJECTS#)
) union all ( 
select /*+ first_rows(1) */
'!' as path,
0 as clevel,
utl_raw.cast_to_raw(pm.vid) as vid,
utl_raw.cast_to_raw(pv.vversion) as vversion,
utl_raw.cast_to_raw(pv.mnecnum) as mnecnum,
utl_raw.cast_to_raw(pv.mndescde) as mndescde,
pv.vstatus as vstatus, 
pv.vuser as vuser,
pv.lockstatus as lockstatus,
pv.lockuser as lockuser,
pv.cmodified as cmodified,
pm.v511parttype as v511parttype,
pv.mnpartstd as mnpartstd,
pm.oid as masterid,
pv.oid as oid
from 
enovia.manpartmaster pm, 
enovia.manpartversion pv 
where pm.oid = pv.vmaster
and pv.oid = hextoraw(:2) ) ) ) 
group by
clevel,
vid, 
vversion, 
mnecnum,
mndescde,
vstatus,
vuser,
lockstatus,
lockuser, 
cmodified,
v511parttype,
mnpartstd,
rawtohex(masterid),
rawtohex(oid)
order by mpath ) 
where rownum <= :3 ) r 
where rfrom > :4  
"""
    .replace("#CHILD#", if (down) "child" else "parent")
    .replace("#PARENT#", if (down) "parent" else "child")
    .replace("#LEVEL#", if (down) "''" else "'-'")

  override lazy val sqlcount = """select count(*) from (
select /*+ result_cache */ 
count(*) as rnum,
vid, 
vversion, 
mnecnum,
mndescde,
vstatus,
vuser,
lockstatus,
lockuser, 
cmodified,
v511parttype,
mnpartstd,
rawtohex(oid) as oid,
min(path) as mpath
from (
select /*+ all_rows */ 
path,
clevel,
vid, 
vversion, 
mnecnum,
mndescde,
vstatus,
vuser,
lockstatus,
lockuser, 
cmodified,
v511parttype,
mnpartstd,
oid
from ( ( 
select /*+ all_rows */ 
path,
clevel,
utl_raw.cast_to_raw(pm.vid) as vid,
utl_raw.cast_to_raw(pv.vversion) as vversion,
utl_raw.cast_to_raw(pv.mnecnum) as mnecnum,
utl_raw.cast_to_raw(pv.mndescde) as mndescde,
pv.vstatus as vstatus, 
pv.vuser as vuser,
pv.lockstatus as lockstatus,
pv.lockuser as lockuser,
pv.cmodified as cmodified,
pm.v511parttype as v511parttype,
pv.mnpartstd as mnpartstd,
pv.oid as oid 
from ( select 
'!'||sys_connect_by_path(trim(to_char(rownum, '0xxxxx')), '!') as path,
level as clevel,
parent, 
child
from ( select 
ar.vari as #PARENT#, 
pm.v514lastversion as #CHILD#
from enovia.vpmassemblyrelation ar, 
enovia.manpartmaster pm
where pm.oid = ar.vpm )
start with parent = hextoraw(:1) 
connect by prior child = parent ),
enovia.manpartversion pv,
enovia.manpartmaster pm
where pv.oid = child
and pv.vmaster = pm.oid
and pm.v514lastversion = pv.oid
and pv.vproject0020 in (#PROJECTS#)
) union all ( 
select /*+ first_rows(1) */
'!' as path,
0 as clevel,
utl_raw.cast_to_raw(pm.vid) as vid,
utl_raw.cast_to_raw(pv.vversion) as vversion,
utl_raw.cast_to_raw(pv.mnecnum) as mnecnum,
utl_raw.cast_to_raw(pv.mndescde) as mndescde,
pv.vstatus as vstatus, 
pv.vuser as vuser,
pv.lockstatus as lockstatus,
pv.lockuser as lockuser,
pv.cmodified as cmodified,
pm.v511parttype as v511parttype,
pv.mnpartstd as mnpartstd,
pv.oid as oid
from 
enovia.manpartmaster pm, 
enovia.manpartversion pv 
where pm.oid = pv.vmaster
and pv.oid = hextoraw(:2) ) ) ) 
group by
vid, 
vversion, 
mnecnum,
mndescde,
vstatus,
vuser,
lockstatus,
lockuser, 
cmodified,
v511parttype,
mnpartstd,
rawtohex(oid)
order by mpath ) 
"""
    .replace("#CHILD#", if (down) "child" else "parent")
    .replace("#PARENT#", if (down) "parent" else "child")

  protected lazy val down = true
  protected lazy val template = "bom"
  protected lazy val version = parameters("version")

  case class Partner(
    row: Int,
    name: NString,
    system: NString,
    partnumber: NString,
    versionstring: NString,
    mansystem: NString,
    manpartnumber: String,
    manversionstring: String,
    description: Option[NString],
    lastmodified: java.sql.Timestamp,
    owner: NString)
    extends PropertiesMapper {

    val datatype = "partners"

    val links = {
      List(
        ("owner", "/users/details/" + owner + "/")).toMap
    }

  }

  private lazy val dummyData = {
    val now = new java.sql.Timestamp(com.ibm.de.ebs.plm.scala.util.Timers.now)
    List(
      Partner(1, "VW Nutzfahrzeuge", "KVS", ".23B.213.183.A", "1", "Enovia5", "85.12340-0102", "_B_", Some("HALTER  F KRAFTSTOFFLEITUNGEN"), now, "C5998"),
      Partner(2, "VW Nutzfahrzeuge", "KVS", ".23B.213.201.", "1", "Enovia5", "85.12201-6101", "_O_", Some("ZSB KRAFTSTOFFBEHAELTER  100L"), now, "C5998"),
      Partner(3, "VW Nutzfahrzeuge", "KVS", ".23B.213.202.B", "1", "Enovia5", "81.12201-5568", "01_", Some("KRAFTSTOFFBEHAELTER  100L"), now, "C5998"),
      Partner(4, "VW Nutzfahrzeuge", "KVS", ".23B.213.203.", "1", "Enovia5", "81.12210-6025", "_A_", Some("ZSB TANKVERSCHLUSS  NICHT ABSPERRBAR UNBELUEFTET"), now, "C5998"),
      Partner(5, "VW Nutzfahrzeuge", "KVS", ".23B.213.204.D", "1", "Enovia5", "81.12210-6029", "_B_", Some("ZSB TANKVERSCHLUSS  F GLEICHSCHIESSUNG BELUEFTET"), now, "C5998"),
      Partner(6, "VW Nutzfahrzeuge", "KVS", ".23B.213.205.", "1", "Enovia5", "81.27203-6016", "06_", Some("ZSB KOMBIGEBER  TX D 385"), now, "C5998"),
      Partner(7, "VW Nutzfahrzeuge", "KVS", ".23B.213.206.", "1", "Enovia5", "81.98181-0227", "01_", Some("VERSCHLUSSTOPFEN  DMR 4MM"), now, "C5998"))
  }

  class Row(result: RichResultSet) extends VersionDetail(
    quantity = result,
    level = result,
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
    project = None,
    lockstatus = result,
    lockuser = result,
    creator = None,
    lastmodified = result,
    created = None,
    assembly = result,
    standardpart = result,
    masterid = result,
    id = result,
    row = None) {

    val partnersystem = partnervalue("system")
    val partnerpartnumber = partnervalue("partnumber")
    val partnerversionstring = partnervalue("versionstring")

    private def partnervalue(attribute: String) = {
      dummyData.find { p => p.manpartnumber.toString.equalsIgnoreCase(name.toString) } match {
        case Some(p) =>
          attribute match {
            case "system" => Some(p.system)
            case "partnumber" => Some(p.partnumber)
            case "versionstring" => Some(p.versionstring)
            case _ => None
          }
        case None => None
      }
    }

  }

  override protected def prepare = statement <<? 2 << version << to << from

  override protected def preparecount(implicit statement: RichPreparedStatement) = statement <<? 2 << version

  override protected def row(result: RichResultSet) = new Row(result)

}
