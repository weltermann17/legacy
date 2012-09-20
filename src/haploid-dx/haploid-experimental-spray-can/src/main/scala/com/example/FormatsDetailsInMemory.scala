package com.example
import com.ibm.haploid.rest.HaploidResource
import cc.spray.RequestContext
import com.example.mixins._

class FormatsDetailsInMemory extends HaploidResource with HasDivision with IsPlm with HasSubsystem with HasTyp with HasValue {

  val pathElement = "formats" / "details"

  def executeRequest(): RequestContext ⇒ Unit = {
    val s = """de.man.mn.gep.scala.config.enovia5.metadata.MetadataServer$Enovia5ResourceFinder$2 -> de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document.FormatsDetailsInMemory
      Division    """ + division + """
      Subsystem   """ + subsystem + """
      Typ         """ + typ + """
      ID          """ + value

    completeWith(s)
  }

}