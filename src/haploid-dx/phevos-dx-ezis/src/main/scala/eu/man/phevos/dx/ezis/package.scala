package eu.man.phevos

package dx

package object ezis {

  import com.ibm.haploid.core.config._

  val ezishostname = getString("phevos.dx.ezis.ezishostname")

  val ezisport = getInt("phevos.dx.ezis.ezisport")

  val typ = getString("phevos.dx.ezis.typ")

  val ezisunstampedpath = getString("phevos.dx.ezis.unstamped.ezispath")

  val ezisunstampedusername = getString("phevos.dx.ezis.unstamped.ezisusername")

  val ezisunstampedpassword = getString("phevos.dx.ezis.unstamped.ezispassword")

  val ezisunstampeduser = getString("phevos.dx.ezis.unstamped.ezisuser")

  val unstampedprog = getString("phevos.dx.ezis.unstamped.prog")

}

