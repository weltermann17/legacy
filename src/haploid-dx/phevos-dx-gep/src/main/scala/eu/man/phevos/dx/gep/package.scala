package eu.man.phevos

package dx

package object gep {

  import com.ibm.haploid.core.config._

  val gephostname = getString("phevos.dx.gep.gephostname")

  val gepport = getInt("phevos.dx.gep.gepport")

  val gepusername = getString("phevos.dx.gep.gepusername")

  val geppassword = getString("phevos.dx.gep.geppassword")

}