package eu.man.phevos.dx

import collection.JavaConversions._

package object kvs {

  import com.ibm.haploid.core.config._

  val responsibleUsers = getConfig("phevos.dx.kvs.responsible-users").entrySet().toList.map(e ⇒
    (e.getKey().substring(5).toInt, e.getValue().render().replace("\"", ""))) sortBy (_._1) reverse

  val defaultVWChangeNumber = getString("phevos.dx.engine.default-vw-change-number")

}