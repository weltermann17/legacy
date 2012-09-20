package de.man.mn.gep.scala.config.enovia5.metadata.server.shared

import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

trait HasFormats {

  this: DatabaseRepresentation =>

  abstract sealed class SubType {
    val name: String
    val connectbyprior: String
    val repeats: Int
    lazy val id: String = parameters(name)
    lazy val template: String = "{" + name + "}"
    lazy val base: String = name + "s/"
  }

  private[server] case class Versions(url: String) extends SubType {

    val name = "version"
    val repeats = 1

    lazy val connectbyprior = """select 
distinct child as vpv
from ( 
select
a.vari as parent, 
v.oid as child,
v.vproject0020 as project
from 
enovia.vpmassemblyrelation a, 
enovia.manpartmaster m,
enovia.manpartversion v
where m.oid = a.vpm
and v.vmaster = m.oid 
and m.v514lastversion = v.oid
)
start with parent = hextoraw(:1) 
connect by prior 
child = parent
and project in (#PROJECTS#)
"""

  }

  private[server] case class Instances(url: String) extends SubType {

    val name = "instance"
    val repeats = 1

    lazy val connectbyprior = """select 
distinct vpv 
from (
select 
oid as child, 
vparentii as parent, 
vpv
from (
select 
i.*, 
v.vproject0020 as project
from 
enovia.maniteminstance i, 
enovia.manpartversion v 
where i.vpv = v.oid )
start with oid = hextoraw(:1)
connect by prior
oid = vparentii
and project in (#PROJECTS#)
and (vshownoshow is null or vshownoshow = 'T') )
"""

  }

  private[server] case class Products(url: String) extends SubType {

    val name = "product"
    val repeats = 3

    lazy val connectbyprior = """select 
distinct vpv
from (
select 
child, 
parent, 
vpv
from (
select
p.oid as child,
null as vpv,
null as vshownoshow,
null as parent
from enovia.manproductrootclass p
where oid = hextoraw(:1)
union all
select 
i.oid as child,
i.vpv as vpv,
i.vshownoshow as vshownoshow,
nvl(i.vparentii, i.vparentprc) as parent 
from 
enovia.maniteminstance i
where i.vparentprc = hextoraw(:2)
)
start with child = hextoraw(:3)
connect by prior
child = parent 
and (vshownoshow is null or vshownoshow = 'T') )
where vpv is not null 
"""

  }

  private[server] case class Snapshots(url: String) extends SubType {

    val name = "snapshot"
    val repeats = 1

    lazy val connectbyprior = """select 
distinct child as vpv
from ( 
select
a.vari as parent, 
v.oid as child,
v.vproject0020 as project
from 
enovia.vpmassemblyrelation a, 
enovia.manpartmaster m,
enovia.manpartversion v
where m.oid = a.vpm
and v.vmaster = m.oid 
and m.v514lastversion = v.oid
)
start with parent = hextoraw(:1) 
connect by prior 
child = parent
and project in (#PROJECTS#)
"""

  }

  lazy val subtype = baseuri match {
    case url if baseuri.contains("versions/") => Versions(url)
    case url if baseuri.contains("instances/") => Instances(url)
    case url if baseuri.contains("products/") => Products(url)
    case url if baseuri.contains("snapshots/") => Snapshots(url)
  }

}