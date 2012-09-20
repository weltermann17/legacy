package eu.man.phevos

package dx

package kvs

package services

import com.ibm.haploid.core.service.{ Success, Result, Failure }
import com.ibm.haploid.core.service.Service

import eu.man.phevos.dx.kvs.KVSException
import eu.man.phevos.dx.util.interfaces.PartInfo

case class ResponsibleUserInput(partInfo: PartInfo)

class ResponsibleUserImpl extends Service[ResponsibleUserInput, String] {

  def doService(in: ResponsibleUserInput): Result[String] = {
    val functionGroup = in.partInfo.mtbPartNumber.substring(3, 5).toInt

    responsibleUsers.find(functionGroup > _._1) match {
      case Some(s) ⇒
        Success(s._2)

      case None ⇒
        Failure(KVSException("No Responsible User found for function group " + functionGroup))
    }
  }

}