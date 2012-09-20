package de.man.mn.gep.scala.config.enovia5.metadata.server.product

import java.io.PrintWriter

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers._
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.ps2Rich
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper

import de.man.mn.gep.scala.config.enovia5.metadata.server.Conversions.origin
import de.man.mn.gep.scala.config.enovia5.metadata.server.ResultSetRepresentation

class Product extends ResultSetRepresentation {

  override lazy val sql = """select /*+ result_cache */ /*+ first_rows(1) */ 
utl_raw.cast_to_raw(vid), 
utl_raw.cast_to_raw(vname||vdescription),
vuser,
vorganization,
utl_raw.cast_to_raw(vproject0004),
lockstatus,
lockuser,
cmodified,
ccreated,
rawtohex(oid),
(select count(b.oid) 
from enovia.manproductrootclass a,
enovia.maniteminstance b
where a.oid = b.vparentprc
and a.oid = hextoraw(:1)),
(select count(*) from (
select distinct vpv  
from enovia.manproductrootclass a,
enovia.maniteminstance b
where a.oid = b.vparentprc
and a.oid = hextoraw(:1)))
from enovia.manproductrootclass a
where a.oid = hextoraw(:1)
and a.vproject0004 in (#PROJECTS#) 
"""

  lazy val product = parameters("product")

  class Row(result: RichResultSet) extends ProductDetail(
    row = None,
    name = result,
    description_de = result,
    owner = result,
    team = result,
    project = result,
    lockstatus = result,
    lockuser = result,
    lastmodified = result,
    created = result,
    id = result,
    instances = result,
    parts = result,
    level = None)

  override protected def prepare = statement <<? 3 << product

  override protected def row(result: RichResultSet) = new Row(result)

}
