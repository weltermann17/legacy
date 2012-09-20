package com.example.mixins
import com.ibm.haploid.rest.path.MixinDescriptors.StaticPathElementDescriptor

trait IsPlm {

  val plmDesc = new StaticPathElementDescriptor("plm")

}