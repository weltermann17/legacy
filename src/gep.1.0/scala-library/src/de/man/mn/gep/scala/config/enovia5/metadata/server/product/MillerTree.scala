package de.man.mn.gep.scala.config.enovia5.metadata.server.product

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

  override lazy val sql = """SELECT
  /*+ result_cache */
  clevel,
  name,
  description_de,
  description_en,
  description_fr,
  description_pl,
  description_tr,
  isassembly,
  oid,
  child
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
    description_en,
    description_fr,
    description_pl,
    description_tr,
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
      utl_raw.cast_to_raw(v.mndescen)    AS description_en,
      utl_raw.cast_to_raw(v.mndescfr)    AS description_fr,
      utl_raw.cast_to_raw(v.mndescpl)    AS description_pl,
      utl_raw.cast_to_raw(v.mndesctr)    AS description_tr,
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
      0                            AS clevel,
      '!'                          AS path,
      NULL                         AS parent,
      p.oid                        AS child,
      p.oid                        AS oid,
      utl_raw.cast_to_raw(p.vid)   AS name,
      utl_raw.cast_to_raw(NULL)    AS instanceid,
      utl_raw.cast_to_raw(p.vname) AS description_de,
      utl_raw.cast_to_raw('')      AS description_en,
      utl_raw.cast_to_raw('')      AS description_fr,
      utl_raw.cast_to_raw('')      AS description_pl,
      utl_raw.cast_to_raw('')      AS description_tr,
      2                            AS isassembly
    FROM ENOVIA.manproductrootclass p
    WHERE p.oid         = hextoraw(:3)
    AND p.vproject0004 IN (#PROJECTS#)
    )
  )
ORDER BY path
"""

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
    oid: String,
    instance_oid: String)
    extends PropertiesMapper {

    override def equals(other: Any): Boolean = instance_oid == other.asInstanceOf[Child].instance_oid
    override def hashCode = oid.hashCode

    var id = newUuid.toString
    var nodes = 0
    val children = new collection.mutable.HashSet[Child]

    val text = description_de match {
      case None => name
      case Some(d) if "DUMMY NUMMER" == d.toString => name
      case Some(d) => d
    }

    def countAllLevels: Int = {
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

      var url = baseuri.substring(0, baseuri.lastIndexOf("graph/")).replace("{product}", oid)
      if (0 != level) {
        url = baseuri.substring(0, baseuri.lastIndexOf("graph/")).replace("{product}", instance_oid).replace("/products/", "/instances/")
      }

      writer.print(Json.build(url))
      writer.print(",\"isassembly\":")
      writer.print(isassembly)
      writer.print("}")
      children.toList.sortWith((a, b) => a.text.toString < b.text).foreach(_.write(writer, id.toString, comma + 1))
      children.clear
    }
  }

  private def level(lvl: Int) = {
    lvl
  }

  private def isassembly(assembly: Int) = {
    2 == assembly
  }

  override def doWrite(writer: PrintWriter) = {
    try {
      var parent: Child = null
      val stack = new collection.mutable.Stack[Child]
      for (
        child <- statement <<? 3 << parameters("product") <<! (result =>
          Child(
            level(result),
            result,
            result,
            result,
            result,
            result,
            result,
            isassembly(result),
            result,
            result))
      ) {
        if (0 < stack.size) {
          while (math.abs(child.level) <= math.abs(stack.top.level)) stack.pop
          stack.top.children += child
          stack.push(child)
        } else if ("0" == node) {
          stack.push(child)
          parent = child
        }
      }

      val root = stack.last
      stack.clear

      if ("0" != node) root.id = node

      writer.print("{\"response\":{\"data\":[")
      root.countAllLevels
      root.write(writer, "root", 0)

      val parents = new MillerTree(parameters ++ Map("node" -> parent.id))(baseuri, connectionfactory)
      writer.print("],\"maxlevelchildren\":")
      writer.print(if (0 < maxlevel) maxlevel else 0)
      writer.print(",\"maxlevelparents\":")
      writer.print(parents.maxlevel)
      writer.print(",\"status\":0}}")

    } catch {
      case e =>
        writer.print("{}")
        throw e
    }
  }
}