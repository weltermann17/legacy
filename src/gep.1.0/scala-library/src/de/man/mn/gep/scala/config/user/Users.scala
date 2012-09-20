package de.man.mn.gep.scala.config.user

import java.io._
import org.restlet._
import org.restlet.data._
import org.restlet.routing._
import org.restlet.resource._
import org.restlet.representation._
import org.restlet.routing.Filter._
import org.restlet.engine.application._
import com.ibm.de.ebs.plm.scala.rest._
import com.ibm.de.ebs.plm.scala.util.Timers._
import com.ibm.de.ebs.plm.scala.rest.Encoding
import de.man.mn.gep.scala._
import com.ibm.de.ebs.plm.scala.rest.Expires
import com.ibm.de.ebs.plm.scala.rest.BasicAuthenticator

case class Users(
  directory: RepresentationDirectoryCache,
  context: Context)
  extends Restlet {

  router.attach("/details/{user}/",
    Encoding(
      CachingRepresentations(
        directory,
        Expires(
          FunctionFilter(
            Ldap.User,
            "user"),
          1 month,
          context),
        1 week,
        context),
      context))

  router.attach("/signon/{user}/{password}/",
    Expires(
      new FunctionFilter(
        Ldap.Password,
        "user",
        "password"),
      1 second,
      context))

  def apply = BasicAuthenticator(router, UserVerifier, context) ++ "/users/signon/"

  private lazy val router = new Router

}
