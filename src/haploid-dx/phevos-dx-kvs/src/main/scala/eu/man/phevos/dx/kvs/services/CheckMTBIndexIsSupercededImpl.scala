package eu.man.phevos.dx.kvs.services
import scala.collection.JavaConversions._
import eu.man.phevos.dx.util.interfaces.PartInfo
import com.ibm.haploid.core.service.Service
import eu.man.phevos.dx.kvs.utils.ResponsibleUser
import com.ibm.haploid.core.service.Result
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.kvs.KVSBaseServices
import com.ibm.haploid.dx.kvs.KVSObject.TYP_T
import com.ibm.haploid.dx.kvs.SelectKStandByKeyInput
import eu.man.phevos.dx.util.interfaces.MTBPartIndexOrdering

case class CheckMTBIndexIsSupercededInput(partinfo: PartInfo)

class CheckMTBIndexIsSupercededImpl

  extends Service[CheckMTBIndexIsSupercededInput, Boolean]

  with ResponsibleUser {

  def doService(in: CheckMTBIndexIsSupercededInput): Result[Boolean] = {

    KVSBaseServices.SelectKStand(SelectKStandByKeyInput(in.partinfo.vwPartNumber, TYP_T, user(in.partinfo))) match {
      case Success(c) if (c.hasPath("KSTAND")) ⇒

        c.getConfigList("KSTAND").toList.find { kstand ⇒
          val mtbIndex = kstand.getString("KSTAND_BESCHREIBUNG").split(" ")(0)
          MTBPartIndexOrdering(mtbIndex) > in.partinfo.mtbPartIndex
        } match {
          case Some(_) ⇒
            Success(true)
          case None ⇒
            Success(false)
        }

      case Success(_) ⇒
        Success(false)
    }

  }

}