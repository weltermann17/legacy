package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rrs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Int
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2NString
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2Timestamp
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.Raw

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.ArrayColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.CompressedColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.UniqueColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.BitSetColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.BooleanColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.HasPermissions
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTableFiller
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.FunctionColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.FunctionColumnA
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Filler
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.LinkTable
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.MostlyNullColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.TableFiller
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document._
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.partner._
import de.man.mn.gep.scala.config.enovia5.metadata.server.Conversions

class VersionsFiller(implicit @transient connectionfactory: ConnectionFactory) extends InMemoryTableFiller[Versions] {

  val sql = """select /*+ all_rows */
(select count(*) from enovia.manpartversion v, enovia.manpartmaster m where m.oid = v.vmaster),
rawtohex(v.oid),
rawtohex(m.oid),
utl_raw.cast_to_raw(m.vid), 
utl_raw.cast_to_raw(v.vversion), 
utl_raw.cast_to_raw(v.mnecnum),
utl_raw.cast_to_raw(v.mndescde),
utl_raw.cast_to_raw(v.mndescen),
utl_raw.cast_to_raw(v.mndescfr),
utl_raw.cast_to_raw(v.mndescpl),
utl_raw.cast_to_raw(v.mndesctr),
utl_raw.cast_to_raw(m.vdescription),
utl_raw.cast_to_raw(v.mnpartmaterial),
utl_raw.cast_to_raw(v.mnpartweight),
v.mnpartweightorig,
v.vstatus,
v.vuser,
m.vuser,
v.vorganization,
v.lockstatus,
v.lockuser, 
v.ccreated,
v.cmodified,
m.v511parttype,
v.mnpartstd,
v.vproject0020
from enovia.manpartversion v, enovia.manpartmaster m where m.oid = v.vmaster
"""

}

class Versions(implicit @transient val connectionfactory: ConnectionFactory, length: Int)
  extends HasPermissions {

  type K = Raw

  type F = VersionsFiller

  val id = new UniqueColumn[Raw]
  val masterid = new ArrayColumn[Raw]
  val name = new UniqueColumn[NString]
  val versionstring = new CompressedColumn[NString]
  val changerequest = new CompressedColumn[NString]
  val description_de = new MostlyNullColumn[NString]
  val description_en = new MostlyNullColumn[NString]
  val description_fr = new MostlyNullColumn[NString]
  val description_pl = new MostlyNullColumn[NString]
  val description_tr = new MostlyNullColumn[NString]
  val descriptiondata = new MostlyNullColumn[NString]
  val material = new MostlyNullColumn[NString]
  val weight = new MostlyNullColumn[NString]
  val weightorigin = new BitSetColumn[String]
  val status = new BitSetColumn[String]
  val owner = new CompressedColumn[String]
  val creator = new CompressedColumn[String]
  val team = new BitSetColumn[String]
  val lockowner = new CompressedColumn[Option[String]]
  val created = new ArrayColumn[java.sql.Timestamp]
  val lastmodified = new ArrayColumn[java.sql.Timestamp]
  val isassembly = new BooleanColumn
  val isstandardpart = new BooleanColumn
  val project = new BitSetColumn[String]

  val instances = new FunctionColumnA[Int, String] {
    val f = (index: Int) => (authorizationidentifier: String) =>
      new TraverseInstances(false).traverse(index, authorizationidentifier)
  }

  val parentversions = new FunctionColumnA[Set[Int], String] {
    val f = (index: Int) => (authorizationidentifier: String) =>
      new TraverseVersions(false, false).traverse(index, authorizationidentifier) + 1
  }

  val versions = new FunctionColumnA[Set[Int], String] {
    val f = (index: Int) => (authorizationidentifier: String) =>
      new TraverseVersions(false, true).traverse(index, authorizationidentifier)
  }

  val documents = new FunctionColumn[Set[Int]] {
    val f = (index: Int) => versiondocuments.distinct(versiondocuments.from.lookup(index), versiondocuments.to)
  }

  val displayname = new FunctionColumn[String] {
    val f = (i: Int) => {
      name(i) +
        " " + versionstring(i) +
        "  " + description_de(i).getOrElse("") +
        " (" + status(i) +
        ", " + owner(i) + ")"
    }
  }

  val filename = new FunctionColumn[String] {
    val f = (i: Int) => name(i).replace(".", "_").replace("-", "_") + "_TYP_0001_" + versionstring(i).replace("-", "_")
  }

  def set(index: Int, row: RichResultSet) = {
    id(index, Raw(row))
    masterid(index, Raw(row))
    name(index, row)
    versionstring(index, row)
    changerequest(index, row)
    description_de(index, row)
    description_en(index, row)
    description_fr(index, row)
    description_pl(index, row)
    description_tr(index, row)
    descriptiondata(index, row)
    material(index, row)
    weight(index, row)
    weightorigin(index, row)
    status(index, Conversions.status(row))
    owner(index, row)
    creator(index, row)
    team(index, row)
    lockowner(index, Conversions.lockowner(row, row))
    created(index, row)
    lastmodified(index, row)
    isassembly(index, Conversions.isassembly(row))
    isstandardpart(index, Conversions.isstandard(row))
    project(index, row)
  }

  val columns = Map(
    "id" -> id,
    "masterid" -> masterid,
    "name" -> name,
    "versionstring" -> versionstring,
    "changerequest" -> changerequest,
    "description_de" -> description_de,
    "description_en" -> description_en,
    "description_fr" -> description_fr,
    "description_pl" -> description_pl,
    "description_tr" -> description_tr,
    "descriptiondata" -> descriptiondata,
    "material" -> material,
    "weight" -> weight,
    "weightorigin" -> weightorigin,
    "status" -> status,
    "owner" -> owner,
    "creator" -> creator,
    "lockowner" -> lockowner,
    "created" -> created,
    "lastmodified" -> lastmodified,
    "isassembly" -> isassembly,
    "isstandardpart" -> isstandardpart,
    "project" -> project,
    "documents" -> documents,
    "displayname" -> displayname,
    "filename" -> filename)

  private lazy val versiondocuments = Repository(classOf[VersionDocuments])

}

