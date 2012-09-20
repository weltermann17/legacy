package eu.man.phevos.dx.kvs.services
import com.ibm.haploid.core.service.Failure
import com.ibm.haploid.core.service.Result
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.service.Service
import com.ibm.haploid.dx.kvs.KVSTypeconverter
import eu.man.phevos.dx.kvs.utils.MaxDVNummer
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.kvs.utils.StringUtils
import eu.man.phevos.dx.util.interfaces.MTBPartMetadata
import eu.man.phevos.dx.util.interfaces.PDA._
import eu.man.phevos.dx.kvs.utils.ResponsibleUser
import eu.man.phevos.dx.util.interfaces.PartInfo

case class GetKVSFilenameForDMUInput(partInfo: PartInfo)

class GetKVSFilenameForDMUImpl

  extends Service[GetKVSFilenameForDMUInput, String]

  with KVSTypeconverter

  with StringUtils

  with MaxDVNummer

  with ResponsibleUser {

  def doService(in: GetKVSFilenameForDMUInput): Result[String] = {
    KVSServices.GetPartName(GetPartNameInput(in.partInfo, PartNameLanguage.English)) match {
      case Success(partName) ⇒
        lazy val docuversions = getDocuversionsByKey(TM.value + ":" + in.partInfo.vwPartNumber, in.partInfo)

        lazy val dvNum = getMaxDVNummer(docuversions)

        lazy val length = if (in.partInfo.vwPartNumberShort.startsWith("TEST")) 72 else 70

        Success(
          replaceSpecialChars(mkLength(mkLength(in.partInfo.vwPartNumber, "_", 14)
            + "_DMU_" + TM.value + "__"
            + mkLength(dvNum.toString, "0", 3, false)
            + "_____"
            + mkLength(partName, "_", 18, truncate = true)
            + "_"
            + in.partInfo.mtbPartNumber
            + "_"
            + in.partInfo.mtbPartIndex
            + "", "_", length)))
    }
  }

}