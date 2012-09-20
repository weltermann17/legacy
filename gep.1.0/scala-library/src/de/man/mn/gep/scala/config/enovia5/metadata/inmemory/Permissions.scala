package de.man.mn.gep.scala.config.enovia5.metadata.inmemory

import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.RichResultSet
import com.ibm.de.ebs.plm.scala.database.ConnectionHelpers.rs2String
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory

trait HasPermissions extends InMemoryTable {

  val project: Column[String]

  @transient implicit private val length = 0
  @transient implicit protected val connectionfactory: ConnectionFactory

  def grantPermission(index: Int)(implicit authorizationidentifier: String) = {
    "DEFAULT" == project(index) || permissions.projects(authorizationidentifier).contains(project(index))
  }

  private def permissions(implicit connectionfactory: ConnectionFactory) = Repository(classOf[Permissions])

}

class PermissionsFiller(implicit @transient connectionfactory: ConnectionFactory) extends InMemoryTableFiller[Permissions] {

  val sql = """select /*+ all_rows */ 
distinct
(select count(*) from (
select distinct
upper(pers.vid),
upper(prj.vid)
from
enovia.rscbase prj,
enovia.rsccontext ctx,
enovia.rscbase pers,
enovia.rscrelationr rsc1,
enovia.rscrelationvcomponentsa rsc2
where prj.type = (select entityuuid from enovia.rdbentitymapping where entityname = 'RscProject')
and prj.oid = ctx.vref1
and ctx.oid = rsc1.vparent
and rsc1.oid = rsc2.oid
and rsc2.value = pers.oid
)),
upper(pers.vid),
upper(prj.vid)
from
enovia.rscbase prj,
enovia.rsccontext ctx,
enovia.rscbase pers,
enovia.rscrelationr rsc1,
enovia.rscrelationvcomponentsa rsc2
where prj.type = (select entityuuid from enovia.rdbentitymapping where entityname = 'RscProject')
and prj.oid = ctx.vref1
and ctx.oid = rsc1.vparent
and rsc1.oid = rsc2.oid
and rsc2.value = pers.oid
"""

}

class Permissions(implicit @transient connectionfactory: ConnectionFactory, length: Int) extends InMemoryTable {

  type K = Int

  type F = PermissionsFiller

  val id = new UniqueColumn[Int]
  val user = new CompressedColumn[String]
  val project = new BitSetColumn[String]

  def projects(u: String): Set[String] = user.lookup(u.toUpperCase).foldLeft(Set[String]())((s, i) => s + project(i))

  def users(p: String): Set[String] = project.lookup(p.toUpperCase).foldLeft(Set[String]())((s, i) => s + user(i))

  def set(index: Int, row: RichResultSet) = {
    id(index, index)
    user(index, row)
    project(index, row)
  }

  val columns = Map(
    "user" -> user,
    "project" -> project)

}

