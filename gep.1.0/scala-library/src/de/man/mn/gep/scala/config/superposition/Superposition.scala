package de.man.mn.gep.scala.config.superposition

import org.restlet.resource.Finder
import org.restlet.resource.ServerResource
import org.restlet.routing.Router

import com.ibm.de.ebs.plm.scala.rest.HasParameters
import com.ibm.de.ebs.plm.scala.rest.UriBuilder

import de.man.mn.gep.scala.Server

class Superposition {

  def apply(router: Router) = {

    case class BasketFinder(uritemplate: String) extends Finder(Server.childContext) with HasParameters {

      override def create(request: org.restlet.Request, response: org.restlet.Response, parameters: Map[String, String]): ServerResource = {
        Basket(parameters)
      }

    }

    object Super extends UriBuilder {
      "/baskets" -> "" --> "/{nicename}/derivedformats/{derivedformat}/" --> { _ => s => BasketFinder(s) }
    }

    Super.attach(router)
  }

}