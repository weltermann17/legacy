package com.example.mixins
import com.ibm.haploid.rest.path.MixinDescriptors.EnumPathElementDescriptor
import com.example.Type

trait HasTyp {

  val typDesc = new EnumPathElementDescriptor(Type)
  var typ: Enumeration#Value = null

}