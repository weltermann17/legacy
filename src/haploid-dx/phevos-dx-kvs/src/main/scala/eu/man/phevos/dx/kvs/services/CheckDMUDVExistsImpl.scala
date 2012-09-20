package eu.man.phevos.dx.kvs.services
import com.ibm.haploid.core.service.Failure
import com.ibm.haploid.core.service.Result
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.service.Service
import com.ibm.haploid.dx.kvs.KVSBaseServices
import com.ibm.haploid.dx.kvs.KVSTypeconverter
import com.ibm.haploid.dx.kvs.SelectDocuVersByAttributesInput
import eu.man.phevos.dx.util.interfaces.MTBPartMetadata
import eu.man.phevos.dx.util.interfaces.PartInfo
import eu.man.phevos.dx.kvs.utils.ResponsibleUser
import eu.man.phevos.dx.util.interfaces.PDA
import eu.man.phevos.dx.kvs.utils.CheckDVExists

case class CheckDMUDVExistsInput(partinfo: PartInfo)

class CheckDMUDVExistsImpl

  extends Service[CheckDMUDVExistsInput, Boolean]

  with ResponsibleUser

  with CheckDVExists {

  def doService(in: CheckDMUDVExistsInput): Result[Boolean] = {
    check(in.partinfo, PDA.TM)
  }

}