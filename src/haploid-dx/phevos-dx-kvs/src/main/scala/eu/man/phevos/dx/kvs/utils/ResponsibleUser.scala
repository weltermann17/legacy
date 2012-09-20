package eu.man.phevos.dx.kvs.utils
import eu.man.phevos.dx.util.interfaces.PartInfo
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.kvs.services.ResponsibleUserInput
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.service.Failure

trait ResponsibleUser {

  def user(partInfo: PartInfo) = {
    KVSServices.ResponsibleUser(ResponsibleUserInput(partInfo)) match {
      case Success(s) ⇒
        s
      case Failure(e) ⇒
        throw e
    }
  }

}