package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product

import com.ibm.de.ebs.plm.scala.database.Raw

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryRepresentation
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository
import de.man.mn.gep.scala.config.enovia5.metadata.server.version.VersionDetail

class VersionInMemory
  extends de.man.mn.gep.scala.config.enovia5.metadata.server.version.Version
  with InMemoryRepresentation {

  override def doWriteInMemory(writer: java.io.PrintWriter) = {

    writer.print("""{"response":{"data":[""")
    try {
      val versions = Repository(classOf[Versions])
      val i = versions.unique(Raw(parameters("version")))
      if (!versions.grantPermission(i)) throw new Exception("Not authorized.")
      val version = new VersionDetail(
        id = versions.id(i).toString,
        masterid = versions.masterid(i).toString,
        name = versions.name(i),
        versionstring = versions.versionstring(i),
        statusstring = versions.status(i),
        owner = versions.owner(i),
        lastmodified = versions.lastmodified(i),
        assembly = (if (versions.isassembly(i)) 2 else 1),
        changerequest = Some(versions.changerequest(i)),
        created = Some(versions.created(i)),
        creator = Some(versions.creator(i)),
        description_de = versions.description_de(i),
        description_en = versions.description_en(i),
        description_fr = versions.description_fr(i),
        description_pl = versions.description_pl(i),
        description_tr = versions.description_tr(i),
        lockstatus = versions.lockowner(i) match { case Some(_) => Some("Y") case None => None },
        lockuser = versions.lockowner(i),
        material = versions.material(i),
        project = Some(versions.project(i)),
        standardpart = Some(if (versions.isstandardpart(i)) "T" else "F"),
        team = Some(versions.team(i)),
        weight = versions.weight(i),
        instances = Some(versions.instances(i).toString),
        parts = Some(versions.versions(i).size.toString),
        parents = Some(versions.parentversions(i).size.toString),
        level = None,
        quantity = None,
        row = None)

      writer.print(version)
      writer.print("""],"startRow":0,"endRow":1,"totalRows":1,"status":0}}""")
    } catch {
      case e =>
        e.printStackTrace
        writer.print("""],"startRow":0,"endRow":0,"totalRows":0,"status":-1}}""")
    }
  }
}