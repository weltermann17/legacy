package eu.man.phevos

package dx

package kvs

package services

import com.ibm.haploid.core.service.{ Success, Result }
import com.ibm.haploid.core.service.Service
import com.ibm.haploid.dx.kvs.{ SelectTeilByKeyInput, KVSBaseServices }

import eu.man.phevos.dx.kvs.utils.ResponsibleUser
import eu.man.phevos.dx.util.interfaces.PartInfo

/**
 *
 */
case class CheckPartNumberExistsInput(partInfo: PartInfo)

class CheckPartNumberExistsImpl

  extends Service[CheckPartNumberExistsInput, Boolean]

  with ResponsibleUser {

  def doService(in: CheckPartNumberExistsInput): Result[Boolean] = {
    KVSBaseServices.SelectTeil(SelectTeilByKeyInput(in.partInfo.vwPartNumber, user(in.partInfo))) match {
      case Success(result) if (!result.hasPath("ERROR")) ⇒
        Success(true)
      case Success(_) ⇒
        Success(false)
    }
  }

}

