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

class MillerTree extends DatabaseRepresentation {

  override lazy val sql = """select /*+ result_cache */ 
clevel,
name, 
description_de,
description_en,
description_fr,
description_pl,
description_tr,
isassembly,
oid
from (
select /*+ all_rows */ 
path,
clevel,
name, 
description_de,
description_en,
description_fr,
description_pl,
description_tr,
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
utl_raw.cast_to_raw(pv.mndescen) as description_en,
utl_raw.cast_to_raw(pv.mndescfr) as description_fr,
utl_raw.cast_to_raw(pv.mndescpl) as description_pl,
utl_raw.cast_to_raw(pv.mndesctr) as description_tr,
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
where pm.oid = ar.vpm)
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
utl_raw.cast_to_raw(pv.mndescen) as description_en,
utl_raw.cast_to_raw(pv.mndescfr) as description_fr,
utl_raw.cast_to_raw(pv.mndescpl) as description_pl,
utl_raw.cast_to_raw(pv.mndesctr) as description_tr,
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

  def this(parameters: Map[String, String])(implicit baseuri: String, connectionfactory: ConnectionFactory) = {
    this()
    init(baseuri, connectionfactory, parameters)
  }

  private lazy val node = parameters("node").toString
  private lazy val up = node.startsWith("P")
  private lazy val down = !up

  var maxlevel = 0

  private[MillerTree] case class Child(
    level: Int,
    name: NString,
    description_de: Option[NString],
    description_en: Option[NString],
    description_fr: Option[NString],
    description_pl: Option[NString],
    description_tr: Option[NString],
    isassembly: Boolean,
    oid: String)
    extends PropertiesMapper {

    override def equals(other: Any): Boolean = oid == other.asInstanceOf[Child].oid
    override def hashCode = oid.hashCode

    var id = (if (0 > level || up) "P" else "") + newUuid
    var nodes = 0
    val children = new collection.mutable.HashSet[Child]

    val text = description_de match {
      case None => name
      case Some(d) if "DUMMY NUMMER" == d.toString => name
      case Some(d) => d
    }

    def countAllLevels: Int = {
      if (up) if (level < maxlevel) maxlevel = level
      if (down) if (level > maxlevel) maxlevel = level
      children.foreach(nodes += _.countAllLevels)
      nodes += children.size
      nodes
    }

    def write(writer: PrintWriter, parentid: String, comma: Int): Unit = {
      def description(d: Option[NString]) = {
        Json.build(d.getOrElse(name) + (if (0 < nodes) " (" + ((if (0 == level) -1 else 0) + nodes) + ")" else ""))
      }
      if (0 < comma) writer.print(",")
      writer.print("{\"id\":\"")
      writer.print(id)
      writer.print("\",\"parentid\":\"")
      writer.print(parentid)
      writer.print("\",\"name\":")
      writer.print(description(Some(name)))
      writer.print(",\"displayname\":")
      writer.print(Json.build(name))
      writer.print(",\"description_de\":")
      writer.print(description(description_de))
      writer.print(",\"description_en\":")
      writer.print(description(description_en))
      writer.print(",\"description_fr\":")
      writer.print(description(description_fr))
      writer.print(",\"description_pl\":")
      writer.print(description(description_pl))
      writer.print(",\"description_tr\":")
      writer.print(description(description_tr))
      writer.print(",\"type\":\"version\"")
      writer.print(",\"level\":")
      writer.print(level)
      writer.print(",\"nodes\":")
      writer.print(nodes)
      writer.print(",\"url\":")
      val url = baseuri.substring(0, baseuri.lastIndexOf("graph/")).replace("{version}", oid)
      writer.print(Json.build(url))
      writer.print(",\"isassembly\":")
      writer.print(isassembly)
      writer.print("}")
      children.toList.sortWith((a, b) => a.text.toString < b.text).foreach(_.write(writer, id.toString, comma + 1))
      children.clear
    }
  }

  private def level(lvl: Int) = {
    if (up) -1 * (lvl + 1) else lvl
  }

  private def isassembly(assembly: Int) = {
    2 == assembly
  }

  private def whereused(child: Child) =
    Child(-1, "Where used", Some("Where used"), Some("Where used"), Some("Where used"), Some("Where used"), Some("Where used"), true, child.oid)

  override def doWrite(writer: PrintWriter) = {
    try {
      var parent: Child = null
      val stack = new collection.mutable.Stack[Child]
      for (
        child <- statement <<? 2 << parameters("version") <<! (result =>
          Child(
            level(result),
            result,
            result,
            result,
            result,
            result,
            result,
            isassembly(result),
            result))
      ) {
        if (0 < stack.size) {
          while (math.abs(child.level) <= math.abs(stack.top.level)) stack.pop
          stack.top.children += child
          stack.push(child)
        } else if ("0" == node && down) {
          parent = whereused(child)
          child.children += parent
          stack.push(child)
        } else if (up) {
          stack.push(whereused(child))
        }
      }
      val root = stack.last
      stack.clear
      if ("0" != node) root.id = node
      if (down) {
        writer.print("{\"response\":{\"data\":[")
        root.countAllLevels
        root.write(writer, "root", 0)
        val parents = new MillerTree(parameters ++ Map("node" -> parent.id))(baseuri, connectionfactory)
        parents.write(writer)
        writer.print("],\"maxlevelchildren\":")
        writer.print(if (0 < maxlevel) maxlevel else 0)
        writer.print(",\"maxlevelparents\":")
        writer.print(parents.maxlevel)
        writer.print(",\"status\":0}}")
      } else if (up) {
        root.countAllLevels
        root.write(writer, node, 1)
      }
    } catch {
      case e =>
        writer.print("{}")
        throw e
    }
  }
}
