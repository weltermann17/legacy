package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory
import com.ibm.de.ebs.plm.scala.database.Raw

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.CompressedColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.UniqueColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.BitSetColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.BooleanColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.FunctionColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.HasPermissions
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryTableFiller
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Matrix
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.MatrixColumn
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository

class AssemblyRelationsFiller(implicit connectionfactory: ConnectionFactory) extends InMemoryTableFiller[AssemblyRelations] {

  val sql = """select /*+ all_rows */     
(select count(*) from enovia.maniteminstance where vlevel = 1 and vpv is not null) + (select count(*) from enovia.vpmassemblyrelation a, enovia.manpartmaster m where a.vpm = m.oid),
rawtohex(oid),
rawtohex(vpv),
'T',
rawtohex(vparentprc),
vmatrix1,
vmatrix2,
vmatrix3,
vmatrix4,
vmatrix5,
vmatrix6,
vmatrix7,
vmatrix8,
vmatrix9,
vmatrix10,
vmatrix11,
vmatrix12    
from enovia.maniteminstance
where vlevel = 1 
and vpv is not null
union all select /*+ all_rows */
(select count(*) from enovia.maniteminstance where vlevel = 1 and vpv is not null) + (select count(*) from enovia.vpmassemblyrelation a, enovia.manpartmaster m where a.vpm = m.oid and a.vari is not null),
rawtohex(a.oid),
rawtohex(m.v514lastversion),
'F',
rawtohex(a.vari),
vmatrix1,
vmatrix2,
vmatrix3,
vmatrix4,
vmatrix5,
vmatrix6,
vmatrix7,
vmatrix8,
vmatrix9,
vmatrix10,
vmatrix11,
vmatrix12    
from enovia.vpmassemblyrelation a,
enovia.manpartmaster m
where a.vpm = m.oid
and a.vari is not null
"""

}

class AssemblyRelations(implicit @transient val connectionfactory: ConnectionFactory, length: Int)
  extends HasPermissions {

  type K = Raw

  type F = AssemblyRelationsFiller

  val id = new UniqueColumn[Raw]
  val child = new CompressedColumn[Int]
  val productparent = new BooleanColumn
  val parent = new CompressedColumn[Int]
  val matrix = new MatrixColumn
  val project = new BitSetColumn[String]
  val isvalid = new FunctionColumn[Boolean] {
    val f = (index: Int) => -1 != child(index) && -1 != parent(index) && (productparent(index) || child(index) != parent(index))
  }

  def set(index: Int, row: RichResultSet) = {
    val versions = Repository.next(classOf[Versions])
    val products = Repository.next(classOf[Products])
    var isproduct = false
    var p = -1
    def setIsProduct(v: String): Boolean = { isproduct = "T" == v; isproduct }
    def setParent(parent: Raw) = { if (isproduct) p = products(parent) else p = versions(parent); p }
    def setProject = if (-1 < p) if (isproduct) products.project(p) else versions.project(p) else ""

    id(index, Raw(row))
    child(index, versions(Raw(row)))
    productparent(index, setIsProduct(row))
    parent(index, setParent(Raw(row)))
    matrix(index, Matrix(row))
    project(index, setProject)
  }

  val columns = Map(
    "id" -> id,
    "child" -> child,
    "productparent" -> productparent,
    "parent" -> parent,
    "matrix" -> matrix,
    "project" -> project,
    "isvalid" -> isvalid)

}

