package com.example.mixins
import com.ibm.haploid.rest.path.MixinDescriptors.EnumPathElementDescriptor
import com.example.Subsystem

trait HasSubsystem {

  val subsystemDesc = new EnumPathElementDescriptor("subsystems", Subsystem)
  var subsystem: Enumeration#Value = null

}