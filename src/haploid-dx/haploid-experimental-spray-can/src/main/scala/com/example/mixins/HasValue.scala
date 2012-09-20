package com.example.mixins
import com.ibm.haploid.rest.path.MixinDescriptors.VariablePathElementDescriptor

trait HasValue {

  val valueDesc = new VariablePathElementDescriptor
  var value: String = null

}