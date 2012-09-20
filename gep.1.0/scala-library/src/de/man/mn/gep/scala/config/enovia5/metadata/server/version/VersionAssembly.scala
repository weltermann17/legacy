package de.man.mn.gep.scala.config.enovia5.metadata.server.version

import de.man.mn.gep.scala.config.enovia5.metadata.server.shared.Assembly

class VersionAssembly extends Assembly {

  override lazy val sql = """select /*+ result_cache */ 
clevel,
m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12,
rawtohex(aggregatedby),
rawtohex(assemblyrelation),
rawtohex(versionid), 
partnumber,
parttype, 
version,
status, 
owner, 
description
from (
select /*+ all_rows */ 
path,
clevel,
m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12,
aggregatedby,
assemblyrelation,
versionid, 
partnumber,
parttype, 
version,
status, 
owner, 
description
from ( ( 
select /*+ all_rows */ 
path,
clevel,
parent as aggregatedby, 
assemblyrelation,
child as versionid, 
m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12,
utl_raw.cast_to_raw(pm.vid) as partnumber,
pm.v511parttype as parttype, 
utl_raw.cast_to_raw(pv.vversion) as version,
pv.vstatus as status, 
pv.vuser as owner, 
utl_raw.cast_to_raw(pv.mndescde) as description
from ( select distinct 
'!'||sys_connect_by_path(trim(to_char(rownum, '0xxxxx')), '!') as path,
level as clevel,
parent, 
assemblyrelation,
child,
m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12
from ( select
ar.vari as parent, 
ar.oid as assemblyrelation,
pm.v514lastversion as child,
vmatrix1 as m1,
vmatrix2 as m2,
vmatrix3 as m3,
vmatrix4 as m4,
vmatrix5 as m5,
vmatrix6 as m6,
vmatrix7 as m7,
vmatrix8 as m8,
vmatrix9 as m9,
vmatrix10 as m10,
vmatrix11 as m11,
vmatrix12 as m12
from 
enovia.vpmassemblyrelation ar, 
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
hextoraw('') as aggregatedby,
hextoraw('') as assemblyrelation,
pv.oid as versionid, 
1.0 as m1, 0.0 as m2, 0.0 as m3, 
0.0 as m4, 1.0 as m5, 0.0 as m6, 
0.0 as m7, 0.0 as m8, 1.0 as m9, 
0.0 as m10, 0.0 as m11, 0.0 as m12,
utl_raw.cast_to_raw(pm.vid) as partnumber,
pm.v511parttype as parttype, 
utl_raw.cast_to_raw(pv.vversion) as version,
pv.vstatus as status, 
pv.vuser as owner, 
utl_raw.cast_to_raw(pv.mndescde) as description
from 
enovia.manpartmaster pm, 
enovia.manpartversion pv 
where pm.oid = pv.vmaster
and pv.oid = hextoraw(:2) ) )
order by path )
"""

  override protected val repeats = 2

}
