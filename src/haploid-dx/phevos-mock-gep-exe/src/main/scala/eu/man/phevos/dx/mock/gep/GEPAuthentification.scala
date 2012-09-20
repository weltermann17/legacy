package eu.man.phevos.dx.mock.gep
import com.ibm.haploid.rest.util.Authentification

trait GEPAuthentification extends Authentification {

  def checkUserPassword(username: String, password: String): Boolean = {
    username.equals("u62xz") && password.equals("datavision")
  }

}