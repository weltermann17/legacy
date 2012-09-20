package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.Raw

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.ReverseLookupColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.FunctionColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.FunctionColumnA
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.HasPermissions
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTableFiller
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository

class InstancesFiller(id: Raw)(implicit connectionfactory: ConnectionFactory)
  extends InMemoryTableFiller[Instances] {

  val sql = """select /*+ all_rows */
(select count(*) from enovia.maniteminstance where oid = hextoraw(:1)),
id,
path,
product,
version, 
parentii,
vlevel,
name
from ( 
   select 
    vparentii as child, 
    oid as parent, 
    sys_connect_by_path(nvl(var, oid), '-') as path, 
    connect_by_isleaf leaf,
    connect_by_root rawtohex(oid) as id,
    connect_by_root rawtohex(vparentprc) as product,
    connect_by_root rawtohex(vpv) as version,
    connect_by_root rawtohex(vparentii) as parentii,
    connect_by_root vlevel as vlevel,
    utl_raw.cast_to_raw(vinstanceid) as name
   from enovia.maniteminstance i
   start with oid = hextoraw(:2) and vparentprc is not null and vpv is not null and (1 = vlevel or var is not null)
   connect by prior vparentii = oid ) 
where 1 = leaf
"""

  override protected def prepare = statement <<? 2 << id.toString

}

class Instances(implicit @transient val connectionfactory: ConnectionFactory, length: Int)
  extends HasPermissions {

  type K = Raw

  type F = InstancesFiller

  val id = new ReverseLookupColumn[Raw] {}
  val path = new ReverseLookupColumn[String] {}
  val product = new ReverseLookupColumn[Int] {}
  val version = new ReverseLookupColumn[Int] {}
  val parent = new ReverseLookupColumn[Raw] {}
  val level = new ReverseLookupColumn[Int] {}
  val name = new ReverseLookupColumn[NString] {}

  val isassembly = new FunctionColumn[Boolean] {
    val f = (index: Int) => versions_.isassembly(version(index))
  }

  val project = new FunctionColumn[String] {
    val f = (index: Int) => versions_.project(version(index))
  }

  val documents = new FunctionColumn[Set[Int]] {
    val f = (index: Int) => versions_.documents(version(index))
  }

  val instances = new FunctionColumnA[Int, String] {
    val f = (index: Int) => (authorizationidentifier: String) => new TraverseInstances(true) {
      override val canTraverse = (_: Input) => {
        val p = path; val i = Instances.this.path(index)
        (i.length < p.length || i.endsWith(p)) && -1 == hiddeninstances.unique(p)
      }
    }.traverse(product(index), authorizationidentifier)
  }

  val versions = new FunctionColumnA[Set[Int], String] {
    val f = (index: Int) => (authorizationidentifier: String) => new TraverseVersions(true, true) {
      override val canTraverse = (_: Input) => {
        val p = path; val i = Instances.this.path(index)
        (i.length < p.length || i.endsWith(p)) && -1 == hiddeninstances.unique(p)
      }
    }.traverse(product(index), authorizationidentifier)
  }

  val displayname = new FunctionColumn[String] {
    val f = (i: Int) => versions_.name(version(i)) + " " + versions_.versionstring(version(i)) + " (" + name(i) + ")"
  }

  val filename = new FunctionColumn[String] {
    val f = (i: Int) => versions_.filename(version(i)) + "_inside_" + products.filename(product(i))
  }

  def set(index: Int, row: RichResultSet) = {
    def convert(s: String): String = {
      val r = new StringBuilder
      var b = 0
      var e = 0
      while (-1 < { b = s.indexOf("-", e); b }) {
        e = s.indexOf("-", b + 1) match { case -1 => s.length case e => e }
        r.append(assemblyrelations(Raw(s.substring(b + 1, e)))).append("-")
      }
      r.toString
    }

    id.set(index, Raw(row))
    path.set(index, convert(row))
    product.set(index, products(Raw(row)))
    version.set(index, versions_(Raw(row)))
    parent.set(index, Raw(row))
    level.set(index, row)
    name.set(index, row)
  }

  val columns = Map(
    "id" -> id,
    "path" -> path,
    "product" -> product,
    "version" -> version,
    "parent" -> parent,
    "level" -> level,
    "name" -> name)

  private lazy val assemblyrelations = Repository(classOf[AssemblyRelations])
  private lazy val products = Repository(classOf[Products])
  private lazy val versions_ = Repository(classOf[Versions])
  private lazy val hiddeninstances = Repository(classOf[HiddenInstances])

}

object Instances {

  def apply(id: Raw)(implicit connectionfactory: ConnectionFactory, m: Manifest[Instances]) = {
    implicit val cons = m.erasure.getConstructors()(0)
    new InstancesFiller(id).fill
  }

}

