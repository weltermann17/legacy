package de.man.mn.gep.scala.config.enovia5.metadata.server.product

import de.man.mn.gep.scala.config.enovia5.metadata.server.shared.Assembly

class ProductAssembly extends Assembly {
  override lazy val sql = """SELECT /*+ result_cache */ /*+ all_rows */ clevel,
  m1,
  m2,
  m3,
  m4,
  m5,
  m6,
  m7,
  m8,
  m9,
  m10,
  m11,
  m12,
  NVL(rawtohex(aggregatedby),''),
  NVL(rawtohex(assemblyrelation),''),
  NVL(rawtohex(versionid),''),
  partnumber,
  parttype,
  version,
  status,
  owner,
  description
FROM
  (SELECT level                                               AS clevel,
    sys_connect_by_path(trim(TO_CHAR(rownum, '0xxxxx')), '!') AS path,
    m1,
    m2,
    m3,
    m4,
    m5,
    m6,
    m7,
    m8,
    m9,
    m10,
    m11,
    m12,
    aggregatedby,
    assemblyrelation,
    versionid,
    partnumber,
    parttype,
    version,
    status,
    owner,
    description
  FROM (
    (SELECT i.oid                       AS chi,
      vshownoshow                       AS vshownoshow,
      a.vari                            AS aggregatedby,
      NVL(i.vparentii, i.vparentprc)    AS par,
      NVL(m.v514lastversion,i.vpv)      AS versionid,
      NVL( a.oid, i.oid)                AS assemblyrelation,
      NVL(a.vmatrix1,i.vmatrix1)        AS m1,
      NVL(a.vmatrix2,i.vmatrix2)        AS m2,
      NVL(a.vmatrix3,i.vmatrix3)        AS m3,
      NVL(a.vmatrix4,i.vmatrix4)        AS m4,
      NVL(a.vmatrix5,i.vmatrix5)        AS m5,
      NVL(a.vmatrix6,i.vmatrix6)        AS m6,
      NVL(a.vmatrix7,i.vmatrix7)        AS m7,
      NVL(a.vmatrix8,i.vmatrix8)        AS m8,
      NVL(a.vmatrix9,i.vmatrix9)        AS m9,
      NVL(a.vmatrix10,i.vmatrix10)      AS m10,
      NVL(a.vmatrix11,i.vmatrix11)      AS m11,
      NVL(a.vmatrix12,i.vmatrix12)      AS m12,
      utl_raw.cast_to_raw(m.vid )       AS partnumber,
      m.v511parttype                    AS parttype,
      utl_raw.cast_to_raw( v.vversion ) AS version,
      v.vstatus                         AS status,
      v.vuser                           AS owner,
      utl_raw.cast_to_raw( v.mndescde ) AS description
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
    (SELECT p.oid                        AS chi,
      'T'                                AS vshownoshow,
      NULL                               AS aggregatedby,
      NULL                               AS par,
      NULL                               AS versionid,
      p.oid                              AS assemblyrelation,
      1.0                                AS m1,
      0.0                                AS m2,
      0.0                                AS m3,
      0.0                                AS m4,
      1.0                                AS m5,
      0.0                                AS m6,
      0.0                                AS m7,
      0.0                                AS m8,
      1.0                                AS m9,
      0.0                                AS m10,
      0.0                                AS m11,
      0.0                                AS m12,
      utl_raw.cast_to_raw(p.vid)         AS partnumber,
      2                                  AS parttype,
      utl_raw.cast_to_raw('   ')         AS version,
      p.vstatus                          AS status,
      p.vuser                            AS owner,
      utl_raw.cast_to_raw (p.vdescription) AS description
    FROM enovia.manproductrootclass p
    WHERE p.oid         = hextoraw(:2)
    AND p.vproject0004 IN (#PROJECTS#)
    ) )
    START WITH chi       = hextoraw(:3)
    CONNECT BY prior chi = par
  AND (vshownoshow      IS NULL
  OR vshownoshow         = 'T')
  ORDER BY path
  )
"""

  override protected val repeats = 3

}