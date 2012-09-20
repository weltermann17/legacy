package com.ibm.haploid.rest
import cc.spray.Route
import cc.spray.Directives
import cc.spray.directives.PathMatcher0
import cc.spray.directives.PathMatcher1
import cc.spray.directives.PathMatcher2
import cc.spray.directives.PathMatcher3
import cc.spray.directives.PathMatcher4
import cc.spray.directives.PathMatcher5
import cc.spray.directives.PathMatcher6
import akka.actor.ActorSystem

private[rest] class DynamicService extends ResourceBuilder with PathBuilder with HaploidService {

  def mixins = restMixins
  def resources = restResources

  lazy val service: Route = {
    var route: Route = null

    resources foreach { resourceName ⇒
      val resource = Class.forName(resourceName).newInstance().asInstanceOf[HaploidResource]
      val (list, pathmatcher) = resourcePath(resource)

      if (pathmatcher.isInstanceOf[PathMatcher0]) {
        route = route.append(path(pathmatcher.asInstanceOf[PathMatcher0])(new ResourceBuilder0(resourceName).completeWith))
      } else if (pathmatcher.isInstanceOf[PathMatcher1[_]]) {
        route = route.append(path(pathmatcher.asInstanceOf[PathMatcher1[_]])(new ResourceBuilder1(resourceName, list).completeWith))
      } else if (pathmatcher.isInstanceOf[PathMatcher2[_, _]]) {
        route = route.append(path(pathmatcher.asInstanceOf[PathMatcher2[_, _]])(new ResourceBuilder2(resourceName, list).completeWith))
      } else if (pathmatcher.isInstanceOf[PathMatcher3[_, _, _]]) {
        route = route.append(path(pathmatcher.asInstanceOf[PathMatcher3[_, _, _]])(new ResourceBuilder3(resourceName, list).completeWith))
      } else if (pathmatcher.isInstanceOf[PathMatcher4[_, _, _, _]]) {
        route = route.append(path(pathmatcher.asInstanceOf[PathMatcher4[_, _, _, _]])(new ResourceBuilder4(resourceName, list).completeWith))
      } else if (pathmatcher.isInstanceOf[PathMatcher5[_, _, _, _, _]]) {
        route = route.append(path(pathmatcher.asInstanceOf[PathMatcher5[_, _, _, _, _]])(new ResourceBuilder5(resourceName, list).completeWith))
      }
    }

    route
  }
}