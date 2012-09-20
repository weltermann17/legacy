package eu.man.phevos.dx.kvs.services
import com.ibm.haploid.core.service.{ Success, Service, Failure }
import com.ibm.haploid.dx.kvs.KVSObject.KSTAND.TYPE
import com.ibm.haploid.dx.kvs.KVSObject._
import com.ibm.haploid.dx.kvs.{ SelectKStandByKeyInput, KVSBaseServices }
import eu.man.phevos.dx.kvs.utils.ResponsibleUser
import eu.man.phevos.dx.util.interfaces.PartInfo
import com.ibm.haploid.dx.kvs.KVSObjectType
import com.ibm.haploid.core.service.Result

case class GetHighestKStandInput(partInfo: PartInfo, typ: KVSObjectType = TYP_T)

class GetHighestKStandImpl

  extends Service[GetHighestKStandInput, Int]

  with ResponsibleUser {

  def doService(in: GetHighestKStandInput): Result[Int] = {
    KVSBaseServices.SelectKStand(SelectKStandByKeyInput(in.partInfo.vwPartNumber, in.typ, user(in.partInfo))) match {
      case Success(s) ⇒
        if (s.hasPath("KSTAND"))
          Success(s.getConfigList("KSTAND").size())
        else
          Success(0)
    }
  }

}