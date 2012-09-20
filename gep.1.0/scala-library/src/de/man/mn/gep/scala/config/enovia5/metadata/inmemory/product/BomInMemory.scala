package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product

import java.io.PrintWriter

import com.ibm.de.ebs.plm.scala.database.Raw
import com.ibm.de.ebs.plm.scala.json.Json.build

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.partner._
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryRepresentation
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.HasPermissions
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Column
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.UnityMatrix

class WhereUsedInMemory extends BomInMemory {

  override val down = false

  override val signum = -1

}

class BomInMemory extends InMemoryRepresentation with Traversing[Int] {

  override def doWriteInMemory(writer: PrintWriter) = {
    this.writer = writer

    writer.print("{\"response\": {")

    writer.print("}}, \"data\":[")
    writeBom

    writer.print("],\"status\":0}}")
  }

  val down = true

  val onlyonce = false

  val groupby = true

  val signum = 1

  lazy val rootproduct = datatype match {
    case "versions" => false
    case "products" | "instances" => true
  }

  lazy val instances = datatype match {
    case "instances" => Instances(Raw(parameters("instance")))
    case _ => null
  }

  lazy val root = datatype match {
    case "versions" => versions(Raw(parameters("version")))
    case "products" => products(Raw(parameters("product")))
    case "instances" => instances.product(0)
  }

  lazy val canTraverse = datatype match {
    case "versions" => (i: Input) => cursor < to
    case "products" => (i: Input) => cursor < to && -1 == hiddeninstances.unique(path)
    case "instances" => (i: Input) =>
      cursor < to && {
        val p = path; val i = instances.path(0)
        (i.length < p.length || i.endsWith(p)) && -1 == hiddeninstances.unique(p)
      }
  }

  lazy val onTraverse = (input: Input) => {
    if (from <= cursor) {
      val assembly = input.assembly
      val parent = relations.parent(assembly)
      val child = relations.child(assembly)
      val productparent = relations.productparent(assembly)

      if (0 == input.depth && 0 == cursor) { // root object
        val index = if (down) parent else child
        println((cursor + 1) + " " + (signum * input.depth) + " " + (if (down && productparent) products.displayname(index) else versions.displayname(index)))
        cursor += 1
      }
      val index = if (down) child else parent
      println((cursor + 1) + " " + (signum * (input.depth + 1)) + " " + (if (up && productparent) products.displayname(index) else versions.displayname(index)) + " " + input.count)

      writer.print("{\"row\":")
      writer.print(cursor)
      writer.print("}")
    }

    cursor += 1
  }

  private def writeBom = {
    traverse(root, (_: Int, _: Int) => 0, 0, authorizationidentifier)
  }

  private var writer: PrintWriter = null

  private var cursor = 0

  private lazy val versions = Repository(classOf[Versions])
  private lazy val products = Repository(classOf[Products])
  private lazy val hiddeninstances = Repository(classOf[HiddenInstances])
  private lazy val partners = Repository(classOf[Partners])
  private lazy val partnermappings = Repository(classOf[PartnerMappings])

}

