package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product

import scala.util.matching.Regex

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.s2nstring
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Timestamp
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.Raw

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.ArrayColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.CompressedColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.UniqueColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.BitSetColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.FunctionColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.FunctionColumnA
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.HasPermissions
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTableFiller
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Filler
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.LinkTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.MostlyNullColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.TableFiller
import de.man.mn.gep.scala.config.enovia5.metadata.server.Conversions

class ProductsFiller(implicit connectionfactory: ConnectionFactory) extends InMemoryTableFiller[Products] {

  val sql = """select /*+ all_rows */
(select count(*) from enovia.manproductrootclass),
rawtohex(oid),
utl_raw.cast_to_raw(vid), 
utl_raw.cast_to_raw(vname),
utl_raw.cast_to_raw(vdescription),
vuser,
vorganization,
vproject0004,
lockstatus,
lockuser,
cmodified,
ccreated
from enovia.manproductrootclass
"""

}

class Products(implicit @transient val connectionfactory: ConnectionFactory, length: Int)
  extends HasPermissions {

  type K = Raw

  type F = ProductsFiller

  val id = new UniqueColumn[Raw]
  val name = new UniqueColumn[NString]
  val alternatename = new MostlyNullColumn[NString]
  val description_de = new MostlyNullColumn[NString]
  val owner = new CompressedColumn[String]
  val team = new CompressedColumn[String]
  val project = new BitSetColumn[String]
  val lockowner = new CompressedColumn[Option[String]]
  val lastmodified = new ArrayColumn[java.sql.Timestamp]
  val created = new ArrayColumn[java.sql.Timestamp]

  val instances = new FunctionColumnA[Int, String] {
    val f = (index: Int) => (authorizationidentifier: String) =>
      new TraverseInstances(true).traverse(index, authorizationidentifier)
  }

  val versions = new FunctionColumnA[Set[Int], String] {
    val f = (index: Int) => (authorizationidentifier: String) =>
      new TraverseVersions(true, true).traverse(index, authorizationidentifier)
  }

  val isassembly = new FunctionColumn[Boolean] {
    val f = (index: Int) => true
  }

  val documents = new FunctionColumn[Set[Int]] {
    val f = (index: Int) => Set[Int]()
  }

  val displayname = new FunctionColumn[String] {
    val f = (index: Int) => name(index) + " " + alternatename(index).getOrElse("") + " " + description_de(index).getOrElse("") + " (" + owner(index) + ")"
  }

  val filename = new FunctionColumn[String] {
    val f = (index: Int) => new Regex("[^A-Za-z0-9\u00C4\u00E4\u00F6\u00D6\u00FC\u00DC\u00DF]").replaceAllIn(name(index), "_")
  }

  def set(index: Int, row: RichResultSet) = {
    id(index, Raw(row))
    name(index, row)
    alternatename(index, row)
    description_de(index, row)
    owner(index, row)
    team(index, row)
    project(index, row)
    lockowner(index, Conversions.lockowner(row, row))
    lastmodified(index, row)
    created(index, row)
  }

  val columns = Map(
    "id" -> id,
    "name" -> name,
    "alternatename" -> alternatename,
    "description_de" -> description_de,
    "owner" -> owner,
    "team" -> team,
    "project" -> project,
    "lockowner" -> lockowner,
    "lastmodified" -> lastmodified,
    "created" -> created,
    "displayname" -> displayname,
    "filename" -> filename)

  private lazy val versions_ = Repository(classOf[Versions])
  private lazy val hiddeninstances = Repository(classOf[HiddenInstances])

}

