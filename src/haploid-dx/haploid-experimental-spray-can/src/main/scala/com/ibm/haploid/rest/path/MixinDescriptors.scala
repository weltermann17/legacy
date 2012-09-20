package com.ibm.haploid.rest.path

object MixinDescriptors {
  abstract class PathDescriptor(val element: String) {
    require(element == null || element.length() > 0)
  }

  case class StaticPathElementDescriptor(val el: String) extends PathDescriptor(el) {
    require(element != null)
  }

  case class EnumPathElementDescriptor(val el: String, val enum: Enumeration) extends PathDescriptor(el) {
    def this(enum: Enumeration) = this(null, enum)
  }

  case class VariablePathElementDescriptor(val el: String = null) extends PathDescriptor(el)
}

trait UsesMixins {

  import MixinDescriptors.PathDescriptor
  import scala.collection.mutable.Map
  import com.ibm.haploid.rest.HaploidResource
  import org.scalastuff.scalabeans.Preamble._

  type IMixinMap = scala.collection.immutable.Map[String, Option[PathDescriptor]]
  type MixinMap = Map[String, Option[PathDescriptor]]

  implicit def mapToMixinsDesc(map: Map[String, Option[PathDescriptor]]): MixinsDesc = new MixinsDesc(map)
  implicit def StringToMixinName(s: String) = new MixinName(s)

  class MixinName(className: String) {

    def toLowercase(s: String): String = {
      s.substring(0, 1).toLowerCase + s.substring(1);
    }

    def name = toLowercase(className.split("[.]").last.replaceFirst("^(Has|Is)", ""))
    def variableName = name
    def description = name + "Desc"
    def variable = name

  }

  class MixinsDesc(map: MixinMap) {

    def getDesc(s: String, resource: HaploidResource): PathDescriptor = {
      val result = map.get(s)

      if (result.get == None) {
        val classDescriptor = descriptorOf(resource.getClass())
        val mixinDescriptor = classDescriptor.get(resource, s.description).asInstanceOf[PathDescriptor]
        map.update(s, Some(mixinDescriptor))
        mixinDescriptor
      } else {
        result.get.get
      }
    }

  }

}