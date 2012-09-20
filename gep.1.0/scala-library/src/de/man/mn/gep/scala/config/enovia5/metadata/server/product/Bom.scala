package de.man.mn.gep.scala.config.enovia5.metadata.server.product

import de.man.mn.gep.scala.config.enovia5.metadata.server.ResultSetRepresentation
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichPreparedStatement
import de.man.mn.gep.scala.config.enovia5.metadata.server.version.VersionDetail

class Bom extends ResultSetRepresentation {

  parent =>

  override lazy val sql = """SELECT r.*
FROM
(SELECT datatype,
vid,
mndescde,
vuser,
lockstatus,
lockuser,
cmodified,
masterid,
oid,
clevel,
v511parttype,
mnpartstd,
rownum AS rfrom, 
rnum,
vversion,
vstatus
FROM
(SELECT
/*+ result_cache */
COUNT(*) AS rnum,
clevel,
vid,
vversion,
mndescde,
mnecnum,
vstatus,
vuser,
lockstatus,
lockuser,
cmodified,
mnpartstd,
v511parttype,
datatype,
rawtohex(masterid) as masterid,
rawtohex(oid) AS oid,
MIN(path)     AS mpath
FROM
(SELECT *
FROM
(SELECT sys_connect_by_path(trim(TO_CHAR(rownum, '0xxxxx')), '!') AS path,
aggregatedby,
assemblyrelation,
versionid,
vid,
vversion,
vstatus,
vuser,
mndescde,
mnecnum,
mnpartstd,
v511parttype,
masterid,
oid,
rownum AS rfrom,
lockuser,
lockstatus,
clevel,
cmodified,
datatype
FROM (
(SELECT i.oid                       AS chi,
  vshownoshow                       AS vshownoshow,
  a.vari                            AS aggregatedby,
  NVL(i.vparentii, i.vparentprc)    AS par,
  NVL(m.v514lastversion,i.vpv)      AS versionid,
  NVL( a.oid, i.oid)                AS assemblyrelation,
  utl_raw.cast_to_raw(m.vid)       	AS vid,
  utl_raw.cast_to_raw(v.vversion) 	AS vversion,
  v.vstatus                         AS vstatus,
  v.vuser                           AS vuser,
  utl_raw.cast_to_raw(v.mndescde) 	AS mndescde,
  utl_raw.cast_to_raw(v.mnecnum)    AS mnecnum,
  v.mnpartstd                       AS mnpartstd,
  m.v511parttype                    AS v511parttype,
  m.oid                             AS masterid,
  v.oid                             AS oid,
  v.lockstatus                      AS lockstatus,
  v.lockuser                        AS lockuser,
  v.cmodified                       AS cmodified,
  i.vlevel                        	AS clevel,
  'versions'						as datatype
FROM enovia.maniteminstance i,
  enovia.vpmassemblyrelation a,
  enovia.manpartversion v,
  enovia.manpartmaster m
WHERE i.vparentprc  = hextoraw(:1)
AND i.var           = a.oid (+)
AND v.oid           = i.vpv
AND m.oid           = v.vmaster
AND v.vproject0020 IN (#PROJECTS#)
)
UNION ALL
(SELECT p.oid                          AS chi,
  'T'                                  AS vshownoshow,
  NULL                                 AS aggregatedby,
  NULL                                 AS par,
  NULL                                 AS versionid,
  p.oid                                AS assemblyrelation,
  utl_raw.cast_to_raw(p.vid)           AS vid,
  utl_raw.cast_to_raw('')              AS vversion,
  p.vstatus                            AS vstatus,
  p.vuser                              AS vuser,
  utl_raw.cast_to_raw(p.vname)  	   AS mndescde,
  utl_raw.cast_to_raw('')              AS mnecnum,
  'F'                                  AS mnpartstd,
  2                                    AS v511parttype,
  null                                 AS masterid,  
  p.oid                                AS oid,
  p.lockstatus                         AS lockstatus,
  p.lockuser                           AS lockuser,
  p.cmodified                          AS cmodified,
  0									   AS clevel,
  'products'						   as datatype
FROM enovia.manproductrootclass p
WHERE p.oid         = hextoraw(:2)
AND p.vproject0004 IN (#PROJECTS#)
) )
START WITH chi       = hextoraw(:3)
CONNECT BY prior chi = par
ORDER BY path
)
)
GROUP BY clevel,
vid,
vversion,
mndescde,
mnecnum,
vstatus,
vuser,
lockstatus,
lockuser,
cmodified,
mnpartstd,
v511parttype,
datatype,
rawtohex(masterid),
rawtohex(oid)
ORDER BY mpath
)
WHERE rownum <= :4
) r
where rfrom > :5"""

  override lazy val sqlcount =
    """SELECT COUNT(instancecount)
FROM
  (SELECT COUNT(i.vpv) AS instancecount,
    i.vpv
  FROM ENOVIA.maniteminstance i
  WHERE i.vparentprc = hextoraw(:1)
  GROUP BY i.vpv
  )
	"""

  class ProductRow(result: RichResultSet) extends ProductDetail(name = result,
    description_de = result,
    owner = result,
    lockstatus = result,
    lockuser = result,
    lastmodified = result,
    masterid = result,
    id = result,
    level = result,
    created = None,
    instances = None,
    team = None,
    project = None,
    row = None,
    parts = None)

  class VersionRow(result: RichResultSet) extends VersionDetail(name = result,
    description_de = result,
    owner = result,
    lockstatus = result,
    lockuser = result,
    lastmodified = result,
    masterid = result,
    id = result,
    level = result,
    assembly = result,
    standardpart = result,
    row = result,
    quantity = result,
    versionstring = result,
    statusstring = result,
    created = None,
    team = None,
    project = None,
    changerequest = None,
    description_en = None,
    description_fr = None,
    description_pl = None,
    description_tr = None,
    material = None,
    weight = None,
    creator = None)

  protected lazy val down = true
  protected lazy val template = "bom"
  protected lazy val product = parameters("product")

  override protected def prepare = statement <<? 3 << product << to << from
  override protected def preparecount(implicit statement: RichPreparedStatement) = statement <<? 1 << product

  override protected def row(result: RichResultSet) = {
    val datatype: String = result
    datatype match {
      case "products" => new ProductRow(result)
      case "versions" => new VersionRow(result)
    }
  }

}