package com.ibm.haploid.dx.engine.domain.marshalling

import javax.xml.bind.JAXBContext
import java.io.File
import javax.xml.bind.annotation.adapters.XmlAdapter
import com.ibm.haploid.dx.engine.event.PersistentEvent
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl
import com.ibm.haploid.dx.engine.domain.binding.ActorPathAdapter
import org.w3c.dom.Node

object Unmarshal {

  def apply(file: File): Any = {
    val xmlcontext = JAXBContext.newInstance(classeswithjaxbbindings: _*)
    val unmarshaller = xmlcontext.createUnmarshaller    
    unmarshaller.unmarshal(file)
  }
  
  def apply(node: Node): Any = {
    val xmlcontext = JAXBContext.newInstance(classeswithjaxbbindings: _*)
    val unmarshaller = xmlcontext.createUnmarshaller    
    unmarshaller.unmarshal(node)
  }

}