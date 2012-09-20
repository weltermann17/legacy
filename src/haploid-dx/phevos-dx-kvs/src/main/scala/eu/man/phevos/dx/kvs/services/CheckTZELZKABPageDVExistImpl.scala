package eu.man.phevos.dx.kvs.services
import eu.man.phevos.dx.util.interfaces.PartInfo
import com.ibm.haploid.core.service.Service
import eu.man.phevos.dx.kvs.utils.ResponsibleUser
import com.ibm.haploid.core.service.Result
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.kvs.KVSBaseServices
import com.ibm.haploid.dx.kvs.SelectDocuVersByKeyInput

case class CheckTZELZKABPageExistsInput(partinfo: PartInfo)

class CheckTZELZKABPageDVExistImpl

  extends Service[CheckTZELZKABPageExistsInput, Boolean]

  with ResponsibleUser {

  def doService(in: CheckTZELZKABPageExistsInput): Result[Boolean] = {
    KVSBaseServices.SelectDocuVers(SelectDocuVersByKeyInput(in.partinfo.vwDefiningIdent + ",Blatt", user(in.partinfo))) match {
      case Success(c) if c.hasPath("DOCUVERS") ⇒
        Success(true)
      case _ ⇒
        Success(false)
    }
  }

}