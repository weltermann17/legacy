package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product

import com.ibm.de.ebs.plm.scala.database.Raw

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryRepresentation
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository
import de.man.mn.gep.scala.config.enovia5.metadata.server.product.ProductDetail

class ProductInMemory
  extends de.man.mn.gep.scala.config.enovia5.metadata.server.product.Product
  with InMemoryRepresentation {

  override def doWriteInMemory(writer: java.io.PrintWriter) = {
    writer.print("""{"response":{"data":[""")
    try {
      val products = Repository(classOf[Products])
      val i = products.unique(Raw(parameters("product")))
      if (!products.grantPermission(i)) throw new Exception("Not authorized.")
      val product = new ProductDetail(
        id = products.id(i).toString,
        name = products.name(i),
        description_de = products.description_de(i),
        owner = products.owner(i),
        team = Some(products.team(i)),
        project = Some(products.project(i)),
        lockstatus = products.lockowner(i) match { case Some(_) => Some("Y") case None => None },
        lockuser = products.lockowner(i),
        lastmodified = products.lastmodified(i),
        created = Some(products.created(i)),
        instances = Some(products.instances(i).toString),
        parts = Some(products.versions(i).size.toString),
        row = None,
        level = None)
      writer.print(product)
      writer.print("""],"startRow":0,"endRow":1,"totalRows":1,"status":0}}""")
    } catch {
      case e =>
        println(e)
        writer.print("""],"startRow":0,"endRow":0,"totalRows":0,"status":-1}}""")
    }
  }

}