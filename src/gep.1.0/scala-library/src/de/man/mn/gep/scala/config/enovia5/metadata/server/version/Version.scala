package de.man.mn.gep.scala.config.enovia5.metadata.server.version

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2Date
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Date
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String

import de.man.mn.gep.scala.config.enovia5.metadata.server.ResultSetRepresentation

class Version extends ResultSetRepresentation {

  override lazy val sql = """select /*+ result_cache */ /*+ first_rows(1) */ 
utl_raw.cast_to_raw(vid), 
utl_raw.cast_to_raw(vversion), 
utl_raw.cast_to_raw(mnecnum),
utl_raw.cast_to_raw(mndescde),
utl_raw.cast_to_raw(mndescen),
utl_raw.cast_to_raw(mndescfr),
utl_raw.cast_to_raw(mndescpl),
utl_raw.cast_to_raw(mndesctr),
utl_raw.cast_to_raw(mnpartmaterial),
utl_raw.cast_to_raw(mnpartweight),
vstatus,
vuser,
vorganization,
utl_raw.cast_to_raw(vproject0020),
lockstatus,
lockuser, 
creator,
cmodified,
ccreated,
v511parttype,
mnpartstd,
rawtohex(masterid),
rawtohex(oid)
from (
select a.*, b.oid as masterid, b.vid, b.v511parttype, b.vuser as creator
from 
enovia.manpartversion a, 
enovia.manpartmaster b 
where a.vmaster = b.oid 
and a.oid = hextoraw(:1)
and a.vproject0020 in (#PROJECTS#) 
)
"""

  class Row(result: RichResultSet) extends VersionDetail(
    row = None,
    name = result,
    versionstring = result,
    changerequest = result,
    description_de = result,
    description_en = result,
    description_fr = result,
    description_pl = result,
    description_tr = result,
    material = result,
    weight = result,
    statusstring = result,
    owner = result,
    team = result,
    project = result,
    lockstatus = result,
    lockuser = result,
    creator = result,
    lastmodified = result,
    created = result,
    assembly = result,
    standardpart = result,
    masterid = result,
    id = result,
    quantity = None,
    level = None)

  override protected def prepare = statement << parameters("version")

  override protected def row(result: RichResultSet) = new Row(result)

}
