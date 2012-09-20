package com.example

import com.ibm.haploid.rest.HaploidService

import cc.spray.directives.PathElement
import cc.spray.Route

class HelloService extends HaploidService {

  val service = {
    val route = pathPrefix("plm") {
      pathPrefix("divisions" / new EnumParam(Division)) { division =>
        pathPrefix("subsystems" / new EnumParam(Subsystem)) { subsystem =>
          path("crt") {
            completeWith("de.man.mn.gep.scala.config.enovia5.metadata.inmemory.crt.CrtInMemory")
          } ~
            pathPrefix("snapshots" / PathElement) { snapshot =>
              path("") {
                completeWith("de.man.mn.gep.scala.config.enovia5.metadata.server.snapshot.Snapshot")
              }
            } ~
            pathPrefix(new EnumParam(Type)) { typ =>
              path("") {
                typ match {
                  case Type.Partnerversion =>
                    completeWith("de.man.mn.gep.scala.config.enovia5.metadata.server.partner.PartnerVersions")
                  case Type.Product =>
                    completeWith("de.man.mn.gep.scala.config.enovia5.metadata.server.product.Products")
                  case Type.Version =>
                    completeWith("de.man.mn.gep.scala.config.enovia5.metadata.server.version.Versions")
                  case _ =>
                    reject()
                }
              } ~
                pathPrefix(PathElement) { id =>
                  path("") {
                    typ match {
                      case Type.Product =>
                        completeWith("de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.ProductInMemory")
                      case Type.Version =>
                        completeWith("de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.VersionInMemory")
                      case Type.Instance =>
                        completeWith("de.man.mn.gep.scala.config.enovia5.metadata.server.instance.Instance")
                      case Type.Partnerversion =>
                        completeWith("de.man.mn.gep.scala.config.enovia5.metadata.server.partner.PartnerVersion")
                      case _ =>
                        reject()
                    }
                  } ~
                    path("assembly" / PathElement / PathElement) { (mapping, prefix) =>
                      completeWith("de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.AssemblyInMemory")
                    } ~
                    path("bom") {
                      CompleteWith(
                          response = "de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.BomInMemory", 
                          when = typ, 
                          isOneOf = Type.Partnerversion, Type.Product, Type.Version)
                    } ~
                    pathPrefix("formats") {
                      path("details") {
                        completeWith("de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document.FormatsDetailsInMemory")
                      } ~
                        path("summary") {
                          completeWith("de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document.FormatsSummaryInMemory")
                        }
                    } ~
                    pathPrefix("graph") {
                      path("millertree" / PathElement) { node =>
                        completeWith("de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.BomInMemory")
                      } ~
                        path("spacetree" / PathElement) { node =>
                          completeWith("de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.SpaceTreeInMemory")
                        }
                    } ~
                    path("instances") {
                      CompleteWith(
                          response = "de.man.mn.gep.scala.config.enovia5.metadata.server.instance.Instance",
                          when = typ,
                          isOneOf = Type.Version
                          )
                    } ~
                    path("iterations") {
                      CompleteWith(
                          response = "de.man.mn.gep.scala.config.enovia5.metadata.inmemory.document.IterationsInMemory",
                          when = typ,
                          isOneOf = Type.Product, Type.Version
                      		)
                    } ~
                    path("products") {
                      CompleteWith(
                          response = "de.man.mn.gep.scala.config.enovia5.metadata.server.product.Products",
                          when = typ,
                          isOneOf = Type.Version
                          )
                    } ~
                    path("whereused") {
                    	CompleteWith(
                    	    response = "de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product.WhereUsedInMemory",
                    	    when = typ,
                    	    isOneOf = Type.Product, Type.Partnerversion
                    			)
                    }
                }
            }
        }
      }
    }

    route
  }

}