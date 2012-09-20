package eu.man.phevos.dx.kvs.services
import com.ibm.haploid.core.service.Result
import com.ibm.haploid.core.service.Service

import eu.man.phevos.dx.kvs.utils.{ ResponsibleUser, CheckDVExists }
import eu.man.phevos.dx.util.interfaces.{ PartInfo, PDA }

case class CheckParametricDocumentVersionAlreadyExistsInput(partInfo: PartInfo)

class CheckParametricDocumentVersionAlreadyExistsImpl

  extends Service[CheckParametricDocumentVersionAlreadyExistsInput, Boolean]

  with ResponsibleUser

  with CheckDVExists {

  def doService(in: CheckParametricDocumentVersionAlreadyExistsInput): Result[Boolean] = {
    check(in.partInfo, PDA.CPL)
  }

}