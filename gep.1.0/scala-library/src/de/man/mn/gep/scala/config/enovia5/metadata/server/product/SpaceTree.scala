package de.man.mn.gep.scala.config.enovia5.metadata.server.product

import java.io.PrintWriter

import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation
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

class SpaceTree extends DatabaseRepresentation {

  override lazy val sql = """SELECT
  /*+ result_cache */
  clevel,
  name,
  description_de,  
  isassembly,  
  oid,
  child,
  instanceid
FROM
  (SELECT
    /*+ all_rows */
    clevel,
    '!'
    ||sys_connect_by_path(trim(TO_CHAR(rownum, '0xxxxx')), '!') AS path,
    parent,
    child,
    oid,
    name,
    instanceid,
    description_de,
    isassembly
  FROM
    (SELECT
      /*+ all_rows */
      i.vlevel                           AS clevel,
      NVL(i.vparentii, i.vparentprc)     AS parent,
      i.oid                              AS child,
      v.oid                              AS oid,
      utl_raw.cast_to_raw(m.vid)         AS name,
      utl_raw.cast_to_raw(i.vinstanceid) AS instanceid,
      utl_raw.cast_to_raw(v.mndescde)    AS description_de,
      m.v511parttype                     AS isassembly
    FROM enovia.maniteminstance i,
      enovia.manpartversion v,
      enovia.manpartmaster m
    WHERE i.vparentprc  = hextoraw(:1)
    AND v.oid           = i.vpv
    AND m.oid           = v.vmaster
    AND v.vproject0020 IN (#PROJECTS#)
    )
    START WITH parent      = hextoraw(:2)
    CONNECT BY prior child = parent
  UNION ALL
    (SELECT
      /*+ first_rows(1) */
      0                                    AS clevel,
      '!'                                  AS path,
      NULL                                 AS parent,
      p.oid                                AS child,
      p.oid                                AS oid,
      utl_raw.cast_to_raw(p.vid)           AS name,
      utl_raw.cast_to_raw(NULL)            AS instanceid,
      utl_raw.cast_to_raw(p.vname) 		   AS description_de,
      2                                    AS isassembly
    FROM ENOVIA.manproductrootclass p
    WHERE p.oid         = hextoraw(:3)
    AND p.vproject0004 IN (#PROJECTS#)
    )
  ) 
ORDER BY path"""

  private var maxrows = 1001
  private val maxlevel = 3
  private var counter = 0

  private lazy val node = {
    val n = parameters("node").toString
    if ("1" == n) {
      maxrows = 10001
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
    oid: String,
    instance_oid: String,
    instance_name: NString)
    extends PropertiesMapper {

    override def equals(other: Any): Boolean = instance_oid == other.asInstanceOf[Child].instance_oid
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

      children.toList.sortWith((a, b) => (a.text.toString + a.instance_name) < (b.text + b.instance_name)).foreach { child =>
        child.write(writer, i)
        i += 1
      }

      children.clear
      writer.print("],\"data\":{\"type\":\"product\",\"isparent\":")
      writer.print(0 > level || up)
      writer.print(",\"$orn\":\"")
      writer.print(if (0 > level || up) "right" else "left")
      writer.print("\",\"description\":")
      var title = text + (if (0 == alllevels) "" else " (" + ((if (0 == level) -1 else 0) + alllevels) + ")")

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
        writer.print(Json.build(baseuri.replace("{product}", oid).replace("{node}", id.toString)))
      }
      writer.print(",\"url\":")

      var url = baseuri.substring(0, baseuri.lastIndexOf("graph/")).replace("{product}", oid)
      if (0 != level) {
        url = baseuri.substring(0, baseuri.lastIndexOf("graph/")).replace("{product}", instance_oid).replace("/products/", "/instances/")
      }

      writer.print(Json.build(url))
      writer.print("}}")
    }
  }

  private def isassembly(assembly: Int) = {
    2 == assembly
  }

  override def doWrite(writer: PrintWriter) = {
    try {
      val stack = new collection.mutable.Stack[Child]
      var i = 0
      for (
        child <- statement <<? 3 << parameters("product") <<! (result =>
          Child(
            result,
            result,
            result,
            isassembly(result),
            result,
            result,
            result))
      ) {

        if (0 < stack.size) {
          while (child.level <= stack.top.level) {
            stack.pop
          }
          stack.top.children += child
        } else if ("0" == node && down) {
          stack.push(child)
        }

        i += 1

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