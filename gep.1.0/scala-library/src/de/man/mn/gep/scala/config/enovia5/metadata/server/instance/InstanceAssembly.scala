package de.man.mn.gep.scala.config.enovia5.metadata.server.instance

import de.man.mn.gep.scala.config.enovia5.metadata.server.shared.Assembly

class InstanceAssembly extends Assembly {

  override lazy val sql = """select /*+ result_cache */ /*+ all_rows */
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
from ( (
select  
path,
clevel,
parent as aggregatedby, 
assemblyrelation,
child as versionid, 
m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12,
utl_raw.cast_to_raw(m.vid) as partnumber,
m.v511parttype as parttype, 
utl_raw.cast_to_raw(v.vversion) as version,
v.vstatus as status, 
v.vuser as owner, 
utl_raw.cast_to_raw(v.mndescde) as description
from ( 
select distinct 
'!ffffff'||sys_connect_by_path(trim(to_char(rownum, '0xxxxx')), '!') as path,
level + ( select vlevel from enovia.maniteminstance where oid = hextoraw(:1) ) as clevel,
parent, 
assemblyrelation,
child,
m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12
from ( select
i.oid as par,
i.vparentii as chi,
i.vshownoshow,
a.vari as parent, 
a.oid as assemblyrelation,
m.v514lastversion as child,
a.vmatrix1 as m1,
a.vmatrix2 as m2,
a.vmatrix3 as m3,
a.vmatrix4 as m4,
a.vmatrix5 as m5,
a.vmatrix6 as m6,
a.vmatrix7 as m7,
a.vmatrix8 as m8,
a.vmatrix9 as m9,
a.vmatrix10 as m10,
a.vmatrix11 as m11,
a.vmatrix12 as m12
from 
enovia.maniteminstance i,
enovia.vpmassemblyrelation a, 
enovia.manpartmaster m
where m.oid = a.vpm 
and a.oid = i.var )
start with chi = hextoraw(:2)
connect by prior par = chi 
and (vshownoshow is null or vshownoshow = 'T') ),
enovia.manpartversion v,
enovia.manpartmaster m
where v.oid = child
and v.vmaster = m.oid
and m.v514lastversion = v.oid 
and v.vproject0020 in (#PROJECTS#)
) union all( 
select
'!'||trim(to_char(i.vlevel, '0xxxxx')) as path,
i.vlevel as clevel,
a.vari as aggregatedby,
a.oid as assemblyrelation,
v.oid as versionid,
a.vmatrix1 as m1,
a.vmatrix2 as m2,
a.vmatrix3 as m3,
a.vmatrix4 as m4,
a.vmatrix5 as m5,
a.vmatrix6 as m6,
a.vmatrix7 as m7,
a.vmatrix8 as m8,
a.vmatrix9 as m9,
a.vmatrix10 as m10,
a.vmatrix11 as m11,
a.vmatrix12 as m12,
utl_raw.cast_to_raw(m.vid) as partnumber,
m.v511parttype as parttype, 
utl_raw.cast_to_raw(v.vversion) as version,
v.vstatus as status, 
v.vuser as owner, 
utl_raw.cast_to_raw(v.mndescde) as description
from 
enovia.vpmassemblyrelation a,
enovia.manpartversion v,
enovia.manpartmaster m,
enovia.maniteminstance i
where a.vpm = m.oid
and m.v514lastversion = v.oid
and v.vproject0020 in (#PROJECTS#)
and v.vmaster = m.oid
and i.var =  a.oid
and i.oid in (
select oid from ( 
select oid, vparentii, vlevel from enovia.maniteminstance 
start with oid = hextoraw(:3)
connect by prior vparentii = oid ) 
where vlevel > 1 )
) union all (
select
'!'||trim(to_char(1, '0xxxxx')) as path,
1 as clevel,
i.vparentprc as aggregatedby,
i.oid as assemblyrelation,
v.oid as versionid,
i.vmatrix1 as m1,
i.vmatrix2 as m2,
i.vmatrix3 as m3,
i.vmatrix4 as m4,
i.vmatrix5 as m5,
i.vmatrix6 as m6,
i.vmatrix7 as m7,
i.vmatrix8 as m8,
i.vmatrix9 as m9,
i.vmatrix10 as m10,
i.vmatrix11 as m11,
i.vmatrix12 as m12,
utl_raw.cast_to_raw(m.vid) as partnumber,
m.v511parttype as parttype, 
utl_raw.cast_to_raw(v.vversion) as version,
v.vstatus as status, 
v.vuser as owner, 
utl_raw.cast_to_raw(v.mndescde) as description
from 
enovia.maniteminstance i,
enovia.manpartmaster m,
enovia.manpartversion v
where i.vpv = v.oid
and v.vmaster = m.oid
and i.oid = (
select oid from ( 
select oid, vparentii, vlevel from enovia.maniteminstance 
start with oid = hextoraw(:4)
connect by prior vparentii = oid ) 
where vlevel = 1 )
) union all (
select 
'!'||trim(to_char(0, '0xxxxx')) as path,
0 as clevel,
hextoraw('') as aggregatedby,
hextoraw('') as assemblyrelation,
p.oid as versionid, 
1.0 as m1, 0.0 as m2, 0.0 as m3, 
0.0 as m4, 1.0 as m5, 0.0 as m6, 
0.0 as m7, 0.0 as m8, 1.0 as m9, 
0.0 as m10, 0.0 as m11, 0.0 as m12,
utl_raw.cast_to_raw(p.vid) as partnumber,
2 as parttype, 
utl_raw.cast_to_raw('   ') as version,
'IN_WORK' as status, 
p.vuser as owner, 
utl_raw.cast_to_raw(p.vdescription) as description
from 
enovia.maniteminstance i,
enovia.manproductrootclass p
where p.oid = i.vparentprc
and i.oid = hextoraw(:5) 
) ) order by path
"""

  override protected val repeats = 5

}
