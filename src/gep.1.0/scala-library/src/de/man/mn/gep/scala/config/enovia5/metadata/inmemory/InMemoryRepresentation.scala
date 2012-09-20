package de.man.mn.gep.scala.config.enovia5.metadata.inmemory

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.Writer

import com.ibm.de.ebs.plm.scala.util.Timers.time

import de.man.mn.gep.scala.config.enovia5.metadata.server.DatabaseRepresentation

trait InMemoryRepresentation extends DatabaseRepresentation {

  def doWriteInMemory(writer: PrintWriter)

  override def write(writer: Writer) = {
    val ms = time {
      doWriteInMemory(writer.asInstanceOf[PrintWriter])
      writer.flush
    }
    println(getClass.getSimpleName + " --> " + ms + " ms")
  }

  override def write(out: OutputStream) = {
    write(new PrintWriter(new OutputStreamWriter(out, "UTF-8")))
  }

  protected def nvl(value: Any) = value match {
    case None => null
    case Some(v) if null == v => null
    case Some(v) => v
    case v if null == v => null
    case v => v
  }

}

