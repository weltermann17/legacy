package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product

import java.io.PrintWriter

import com.ibm.de.ebs.plm.scala.database.Raw
import com.ibm.de.ebs.plm.scala.json.Json.build

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.partner.PartnerMappings
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.partner.Partners
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryRepresentation
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.HasPermissions
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Column
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.UnityMatrix

class AssemblyInMemory extends InMemoryRepresentation with Traversing[Int] {

  override def doWriteInMemory(writer: PrintWriter) = {
    this.writer = writer

    writer.print("{\"response\": {")

    writer.print("\"properties\": {")
    writeProperties(writer)

    writer.print("}}, \"instances\":[")
    writeInstances

    writer.print("],\"versions\":[")
    writeRoot(root)
    writeReferences
    writer.print("],\"status\":0}}")
  }

  val down = true

  val onlyonce = true

  val groupby = false

  lazy val instances = datatype match {
    case "instances" => Instances(Raw(parameters("instance")))
    case _ => null
  }

  lazy val root = datatype match {
    case "versions" => versions(Raw(parameters("version")))
    case "products" => products(Raw(parameters("product")))
    case "instances" => instances.product(0)
  }

  lazy val rootproduct = datatype match {
    case "versions" => false
    case "products" | "instances" => true
  }

  lazy val canTraverse = datatype match {
    case "versions" => (i: Input) => true
    case "products" => (i: Input) => -1 == hiddeninstances.unique(path)
    case "instances" => (i: Input) =>
      val p = path; val i = instances.path(0)
      (i.length < p.length || i.endsWith(p)) && -1 == hiddeninstances.unique(p)
  }

  lazy val onTraverse = (input: Input) => {
    val assembly = input.assembly
    val parent = relations.parent(assembly)
    val child = relations.child(assembly)

    val instanceof = references.get(child) match {
      case None => referencecounter += 1; references.put(child, referencecounter); referencecounter
      case Some(id) => id
    }

    val aggregatedby = if (0 == input.depth) 1 else references.getOrElse(parent, -1)

    if (-1 == aggregatedby) {
      throw new IndexOutOfBoundsException("parent not found " + parent)
    }

    writer.print(",{")
    relations.matrix(assembly).print(writer)
    writer.print(",\"instanceof\":" + instanceof)
    writer.print(",\"aggregatedby\":" + aggregatedby)
    writer.print("}")
  }

  lazy val writeRoot = datatype match {
    case "versions" => (i: Int) => VersionReference(i).print(writer)
    case "products" | "instances" => (i: Int) => ProductReference(i).print(writer)
  }

  lazy val writeReference = (i: Int) => { writer.print(","); VersionReference(i).print(writer) }

  private def writeInstances = {
    writer.print("{")
    UnityMatrix.print(writer)
    writer.print(",\"instanceof\":1,\"aggregatedby\":0}")
    traverse(root, (_: Int, _: Int) => 0, 0, authorizationidentifier)
  }

  private var writer: PrintWriter = null

  private def writeReferences = references.keySet.foreach(writeReference)

  private def writeProperties(writer: PrintWriter) = {

    def getEntry(row: Int) = { "\"" + partnermappings.property(row).get + "\": \"" + partnermappings.attribute(row).get + "\"" }

    def writeSet(writer: PrintWriter, propertyset: Set[String]) = {
      var i = 0
      propertyset.foreach { entry =>
        if (0 < i) writer.print(",")
        writer.print(entry)
        i += 1
      }
    }

    val properties = partnermappings.mapping.lookup("catia5")
    var general = Set[String]()
    var products = Set[String]()
    var versions = Set[String]()

    general += ("\"partner\": \"" + partners.name(partners.unique(Raw(parameters("partner")))) + "\"")
    properties.foreach(i => {
      if (partnermappings.fromtable(i) != None) {
        if (partnermappings.fromtable(i).get.equals("general")) general += getEntry(i)
        if (partnermappings.fromtable(i).get.equals("products")) products += getEntry(i)
        if (partnermappings.fromtable(i).get.equals("versions")) versions += getEntry(i)
      }
    })

    writer.print("\"general\": {")
    writeSet(writer, general)
    writer.print("}, \"parts\":{")
    writeSet(writer, versions)
    writer.print("}, \"products\":{")
    writeSet(writer, products)
  }

  private lazy val mapping = {
    val partner = partners.unique(Raw(parameters("partner")))
    partners.mappings(partner)("enovia5").foldLeft(List[(Option[String], Option[String], Option[String])]()) {
      case (l, i) => (partnermappings.fromtable(i),
        partnermappings.fromcolumn(i),
        partnermappings.attribute(i)) :: l
    }
  }

  private abstract class AssemblyReference[T <: InMemoryTable](
    table: T {
      val displayname: Column[String];
      val filename: Column[String];
      val isassembly: Column[Boolean]
    }) {

    val index: Int
    val id = references.getOrElse(index, 1)
    val displayname = table.displayname(index)
    val filename = table.filename(index)
    val isassembly = table.isassembly(index)

    def attributes(writer: PrintWriter) = {
      val attr = mapping.foldLeft(List[(String, Any)]()) {
        case (l, (Some(tablename), Some(column), Some(attribute))) if tablename.equalsIgnoreCase(table.shortname) =>
          if (table.columns.contains(column)) {
            (attribute, nvl(table.columns(column)(index))) :: l
          } else {
            l
          }
        case (l, _) => l
      }.filter(null != _._2).toMap
      if (0 < attr.size) {
        writer.print(",\"attributes\":")
        writer.print(build(attr))
      }
    }

    def print(writer: PrintWriter) = {
      writer.print("{\"id\":")
      writer.print(id)
      writer.print(",\"displayname\":")
      writer.print(build(displayname))
      writer.print(",\"filename\":")
      writer.print(build(filename))
      writer.print(",\"isassembly\":")
      writer.print(isassembly)
      attributes(writer)
      writer.print("}")
    }
  }

  private case class VersionReference(val index: Int) extends AssemblyReference(versions)

  private case class ProductReference(val index: Int) extends AssemblyReference(products)

  private lazy val references = new collection.mutable.HashMap[Int, Int]
  private var referencecounter = 1

  private lazy val versions = Repository(classOf[Versions])
  private lazy val products = Repository(classOf[Products])
  private lazy val hiddeninstances = Repository(classOf[HiddenInstances])
  private lazy val partners = Repository(classOf[Partners])
  private lazy val partnermappings = Repository(classOf[PartnerMappings])

}

