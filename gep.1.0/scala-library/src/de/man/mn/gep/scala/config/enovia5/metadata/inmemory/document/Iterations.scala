package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document

import java.io.PrintWriter

import org.restlet.data.MediaType

import com.ibm.de.ebs.plm.scala.database.Raw
import com.ibm.de.ebs.plm.scala.rest.Services
import com.ibm.de.ebs.plm.scala.util.Converters.bytesToKb
import com.ibm.de.ebs.plm.scala.util.Converters.bytesToMb

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.Products
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.Versions
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.InMemoryRepresentation
import de.man.mn.gep.scala.config.enovia5.metadata.inmemory.Repository

class IterationsInMemory
  extends InMemoryRepresentation {

  override def doWriteInMemory(writer: PrintWriter) = {
    val children = datatype match {
      case "versions" => val v = versions(Raw(parameters("version"))); versions.versions(v)
      case "products" => val p = products(Raw(parameters("product"))); products.versions(p)
    }

    writer.print("{\"response\":{\"data\":[")
    var i = 0
    children.foreach { v =>
      versions.documents(v).foreach { d =>
        if (0 < i) writer.print(",")
        writer.print("\"");
        writer.print(documents.iteration(d))
        writer.print("\"");
        i += 1
      }
    }
    writer.print("],\"startRow\":")
    writer.print(0)
    writer.print(",\"endRow\":")
    writer.print(i)
    writer.print(",\"totalRows\":")
    writer.print(i)
    writer.print(",\"status\":0}}")
  }

  private lazy val products = Repository(classOf[Products])
  private lazy val versions = Repository(classOf[Versions])
  private lazy val documents = Repository(classOf[Documents])

}

