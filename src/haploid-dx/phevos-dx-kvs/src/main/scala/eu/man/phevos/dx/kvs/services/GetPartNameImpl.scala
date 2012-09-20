package eu.man.phevos

package dx

package kvs

package services

import com.ibm.haploid.core.service.{ Success, Result, Failure }
import com.ibm.haploid.core.service.Service
import com.ibm.haploid.dx.kvs.{ SelectTeilByKeyInput, KVSBaseServices }

import eu.man.phevos.dx.kvs.utils.ResponsibleUser
import eu.man.phevos.dx.kvs.KVSException
import eu.man.phevos.dx.util.interfaces.PartInfo

sealed abstract class PartNameLanguage(val value: String) {
  def this() = this(null)
}

object PartNameLanguage {

  case object English extends PartNameLanguage("EN.TEIL_BENENNUNG")
  case object Portuguese extends PartNameLanguage("PT.TEIL_BENENNUNG")

}

case class GetPartNameInput(partInfo: PartInfo, lang: PartNameLanguage)

class GetPartNameImpl

  extends Service[GetPartNameInput, String]

  with ResponsibleUser {

  def doService(in: GetPartNameInput): Result[String] = {

    KVSBaseServices.SelectTeil(SelectTeilByKeyInput(in.partInfo.vwPartNumber, user(in.partInfo), Some(4))) match {
      case Success(result) ⇒
        val teil = result.getConfigList("TEIL").get(0)
        if (teil.hasPath(in.lang.value))
          Success(teil.getString(in.lang.value))
        else
          Success("")
      case Failure(e) ⇒
        Failure(KVSException("Teil with key " + in.partInfo.vwPartNumber + " doesen't exist in KVS."))
    }

  }

}