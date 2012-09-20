package com.example.mixins
import com.ibm.haploid.rest.path.MixinDescriptors.EnumPathElementDescriptor
import com.example.Division

trait HasDivision {

  val divisionDesc = new EnumPathElementDescriptor("divisions", Division)
  var division: Enumeration#Value = null

}