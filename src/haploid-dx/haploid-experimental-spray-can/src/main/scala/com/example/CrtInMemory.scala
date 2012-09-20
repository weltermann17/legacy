package com.example
import com.ibm.haploid.rest.HaploidResource
import com.example.mixins.HasDivision
import com.example.mixins.HasSubsystem
import com.example.mixins.IsPlm
import cc.spray.directives.PathMatcher0
import cc.spray.RequestContext

class CrtInMemory extends HaploidResource with IsPlm with HasDivision with HasSubsystem {

  val pathElement: PathMatcher0 = "crt"

  def executeRequest(): RequestContext ⇒ Unit = {
    val s = """de.man.mn.gep.scala.config.enovia5.metadata.MetadataServer$Enovia5ResourceFinder$2 -> de.man.mn.gep.scala.config.enovia5.metadata.inmemory.crt.CrtInMemory
      Division    """ + division + """
      Subsystem   """ + subsystem

    completeWith(s)
  }

}