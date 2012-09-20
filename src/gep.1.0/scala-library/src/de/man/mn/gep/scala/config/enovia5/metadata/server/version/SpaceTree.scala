package de.man.mn.gep.scala.config.enovia5.metadata.server.version

import java.io.PrintWriter

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.nstring2s
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.ps2Rich
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.s2nstring
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.PropertiesMapper
import com.ibm.de.ebs.plm.scala.json.Json
import com.ibm.de.ebs.plm.scala.text.Uuid.newUuid

import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

class SpaceTree extends DatabaseRepresentation {

  override lazy val sql = """select /*+ result_cache */ 
clevel,
name, 
description_de,
isassembly,
oid
from (
select /*+ all_rows */ 
path,
clevel,
name, 
description_de,
isassembly,
rawtohex(child) as oid
from ( ( 
select /*+ all_rows */ 
path,
clevel,
parent, 
child, 
utl_raw.cast_to_raw(pm.vid||' '||pv.vversion) as name, 
utl_raw.cast_to_raw(pv.mndescde) as description_de,
pm.v511parttype as isassembly 
from ( select 
'!'||sys_connect_by_path(trim(to_char(rownum, '0xxxxx')), '!') as path,
level as clevel, 
parent, 
child
from ( select
ar.vari as #PARENT#, 
pm.v514lastversion as #CHILD#
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
null as parent,
pv.oid as child,
utl_raw.cast_to_raw(pm.vid||' '||pv.vversion) as name, 
utl_raw.cast_to_raw(pv.mndescde) as description_de,
pm.v511parttype as isassembly 
from 
enovia.manpartmaster pm, 
enovia.manpartversion pv 
where pm.oid = pv.vmaster
and pv.oid = hextoraw(:2) ) )
order by path )
"""
    .replace("#CHILD#", if (down) "child" else "parent")
    .replace("#PARENT#", if (down) "parent" else "child")

  private var maxrows = 1001
  private val maxlevel = 3
  private var counter = 0

  private lazy val node = {
    val n = parameters("node").toString
    if ("1" == n) {
      maxrows = Int.MaxValue
      "0"
    } else n
  }
  private lazy val up = node.startsWith("P")
  private lazy val down = !up

  private[SpaceTree] case class Child(
    level: Int,
    name: NString,
    description: Option[NString],
    isassembly: Boolean,
    oid: String)
    extends PropertiesMapper {

    override def equals(other: Any): Boolean = oid == other.asInstanceOf[Child].oid
    override def hashCode = oid.hashCode

    var id = (if (0 > level || up) "P" else "") + newUuid
    var alllevels = 0
    var firstlevel = 0
    val text = description match {
      case None => name
      case Some(d) if "DUMMY NUMMER" == d.toString => name
      case Some(d) => d
    }
    val children = new collection.mutable.HashSet[Child]

    def countAllLevels: Int = {
      children.foreach(alllevels += _.countAllLevels)
      firstlevel = children.size
      alllevels += firstlevel
      alllevels
    }

    def thinOut(level: Int): Boolean = {
      counter += 1
      val keep = maxrows > counter || maxlevel > level
      var keptall = true
      children.foreach(keptall &&= _.thinOut(level + 1))
      if (!keptall) children.clear
      keep
    }

    def write(writer: PrintWriter, comma: Int): Unit = {
      if (0 < comma) writer.print(",")
      writer.print("{\"id\":\"")
      writer.print(id)
      writer.print("\",\"name\":")
      writer.print(Json.build(name))
      writer.print(",\"children\":[")
      val csize = children.size
      var i = 0
      children.toList.sortWith((a, b) => a.text.toString < b.text).foreach { child =>
        child.write(writer, i)
        i += 1
      }
      children.clear
      writer.print("],\"data\":{\"type\":\"version\",\"isparent\":")
      writer.print(0 > level || up)
      writer.print(",\"$orn\":\"")
      writer.print(if (0 > level || up) "right" else "left")
      writer.print("\",\"description\":")
      val title = text + (if (0 == alllevels) "" else " (" + ((if (0 == level) -1 else 0) + alllevels) + ")")
      val len = title.length
      writer.print(Json.build(title))
      writer.print(",\"$height\":")
      writer.print(if (54 < title.length) 64 else if (36 < title.length) 48 else if (18 < title.length) 32 else 20)
      writer.print(",\"firstlevel\":")
      writer.print(firstlevel)
      writer.print(",\"firstlevelcomplete\":")
      writer.print(-1 != level && firstlevel == csize)
      writer.print(",\"alllevels\":")
      writer.print(alllevels)
      writer.print(",\"isassembly\":")
      writer.print(isassembly)
      if (isassembly) {
        writer.print(",\"subtree\":")
        writer.print(Json.build(baseuri.replace("{version}", oid).replace("{node}", id.toString)))
      }
      writer.print(",\"url\":")
      val url = baseuri.substring(0, baseuri.lastIndexOf("graph/")).replace("{version}", oid)
      writer.print(Json.build(url))
      writer.print("}}")
    }
  }

  private def isassembly(assembly: Int) = {
    2 == assembly
  }

  private def whereused(child: Child) = Child(-1, "Where used", Some("Where used"), true, child.oid)

  override def doWrite(writer: PrintWriter) = {
    try {
      val stack = new collection.mutable.Stack[Child]
      for (
        child <- statement <<? 2 << parameters("version") <<! (result =>
          Child(
            result,
            result,
            result,
            isassembly(result),
            result))
      ) {
        if (0 < stack.size) {
          while (child.level <= stack.top.level) stack.pop
          stack.top.children += child
        } else if ("0" == node && down) {
          child.children += whereused(child)
        } else if (up) {
          stack.push(whereused(child))
          stack.top.children += child
        }
        stack.push(child)
      }
      val root = stack.last
      stack.clear
      if ("0" != node) root.id = node
      root.countAllLevels
      if (root.alllevels < 1.2 * maxrows) maxrows = math.round(1.2f * maxrows)
      root.thinOut(0)
      root.write(writer, 0)
    } catch {
      case e =>
        writer.print("{}")
        throw e
    }
  }
}
