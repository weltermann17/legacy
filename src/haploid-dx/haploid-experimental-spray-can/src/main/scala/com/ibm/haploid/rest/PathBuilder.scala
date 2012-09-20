package com.ibm.haploid.rest

import scala.collection.mutable.Map
import org.scalastuff.scalabeans.Preamble._
import com.ibm.haploid.rest.path.MixinDescriptors._
import com.ibm.haploid.rest.path.UsesMixins
import cc.spray.directives.IntNumber
import cc.spray.directives.PathElement
import cc.spray.directives.PathMatcher
import cc.spray.directives.PathMatcher0
import cc.spray.directives.PathMatcher1
import cc.spray.directives.PathMatcher2
import cc.spray.directives.PathMatcher3
import cc.spray.directives.PathMatcher4
import cc.spray.directives.PathMatcher5
import cc.spray.directives.PathMatcher6
import cc.spray.directives.PathMatcher7
import cc.spray.Directives
import cc.spray.Route
import cc.spray.directives.LongNumber
import cc.spray.directives.SimpleRegexMatcher
import com.example.Division

private[rest] trait PathBuilder extends UsesMixins with Directives {

  def mixins: List[String]
  val mixinsDesc: MixinMap = Map((mixins map { s ⇒ (s, None) } toMap).toSeq: _*)

  class ExtendedRoute(route: Route) {

    def append(routeToAppend: Route): Route = {
      if (route == null) {
        routeToAppend
      } else {
        route ~ routeToAppend
      }
    }

  }

  implicit def routeToExtendedRoute(route: Route) = new ExtendedRoute(route)

  class EnumElement(val enum: Enumeration) extends PathMatcher1[Enumeration#Value] {
    private val regexMatcher = new SimpleRegexMatcher(""".*""".r)

    def apply(path: String) = {
      val tuple = PathElement.apply(path).get
      val value = tuple._2._1

      try {
        Some(tuple._1, Tuple1(enum.withName(value)))
      } catch {
        case e: Throwable ⇒ None
      }
    }
  }

  def resourcePath(resource: HaploidResource): (List[String], PathMatcher[_ <: Product]) = {
    val resourceDesc = descriptorOf(resource.getClass());
    val propertyNames = resourceDesc.properties.mapConserve(_.name)
    var properties = List[String]()
    var path: PathMatcher[_ <: Product] = null

    def extend(path: PathMatcher[_ <: Product], s: PathMatcher0): PathMatcher[_ <: Product] = {
      if (path == null) {
        s
      } else if (path.isInstanceOf[PathMatcher0]) {
        path.asInstanceOf[PathMatcher0] / s
      } else if (path.isInstanceOf[PathMatcher1[_]]) {
        path.asInstanceOf[PathMatcher1[_]] / s
      } else if (path.isInstanceOf[PathMatcher2[_, _]]) {
        path.asInstanceOf[PathMatcher2[_, _]] / s
      } else if (path.isInstanceOf[PathMatcher3[_, _, _]]) {
        path.asInstanceOf[PathMatcher3[_, _, _]] / s
      } else if (path.isInstanceOf[PathMatcher4[_, _, _, _]]) {
        path.asInstanceOf[PathMatcher4[_, _, _, _]] / s
      } else if (path.isInstanceOf[PathMatcher5[_, _, _, _, _]]) {
        path.asInstanceOf[PathMatcher5[_, _, _, _, _]] / s
      } else if (path.isInstanceOf[PathMatcher6[_, _, _, _, _, _]]) {
        path.asInstanceOf[PathMatcher6[_, _, _, _, _, _]] / s
      } else if (path.isInstanceOf[PathMatcher7[_, _, _, _, _, _, _]]) {
        path.asInstanceOf[PathMatcher7[_, _, _, _, _, _, _]] / s
      } else {
        // TODO Add Exception
        null
      }
    }

    def extendWithVariable(path: PathMatcher[_ <: Product], s: PathMatcher1[_]): PathMatcher[_ <: Product] = {
      if (path == null) {
        s
      } else if (path.isInstanceOf[PathMatcher0]) {
        path.asInstanceOf[PathMatcher0] / s
      } else if (path.isInstanceOf[PathMatcher1[_]]) {
        path.asInstanceOf[PathMatcher1[_]] / s
      } else if (path.isInstanceOf[PathMatcher2[_, _]]) {
        path.asInstanceOf[PathMatcher2[_, _]] / s
      } else if (path.isInstanceOf[PathMatcher3[_, _, _]]) {
        path.asInstanceOf[PathMatcher3[_, _, _]] / s
      } else if (path.isInstanceOf[PathMatcher4[_, _, _, _]]) {
        path.asInstanceOf[PathMatcher4[_, _, _, _]] / s
      } else {
        // TODO Add Exception
        null
      }
    }

    mixins foreach { mixin ⇒

      if (propertyNames.contains(mixin.description)) {
        val mixinDesc = mixinsDesc.getDesc(mixin, resource)

        mixinDesc match {
          case StaticPathElementDescriptor(element) ⇒
            path = extend(path, element)
          case EnumPathElementDescriptor(element, enum) ⇒
            if (element != null) {
              path = extend(path, element)
            }

            path = extendWithVariable(path, new EnumElement(enum))
            properties = properties ++ List(mixin.variableName)
          case VariablePathElementDescriptor(element) ⇒
            element
            if (element != null) {
              path = extend(path, element)
            }

            val valueDesc = resourceDesc.property(mixin.variableName)
            require(valueDesc != None)

            valueDesc.get.scalaType.toString() match {
              case "String" ⇒
                path = extendWithVariable(path, PathElement)
              case "int" ⇒
                path = extendWithVariable(path, IntNumber)
              case "long" ⇒
                path = extendWithVariable(path, LongNumber)
              case other ⇒
                throw new IllegalArgumentException("Datatype " + other + " is not supported.")

            }

            properties = properties ++ List(mixin.variableName)
        }
      }

    }

    if (resource.pathElement != null)
      path = extend(path, resource.pathElement)

    (properties, path)
  }

}