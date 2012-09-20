package eu.man.phevos.dx.kvs.services
import scala.collection.JavaConversions.asScalaBuffer
import com.ibm.haploid.core.service.Failure
import com.ibm.haploid.core.service.Result
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.service.Service
import com.ibm.haploid.dx.kvs.KVSBaseServices
import com.ibm.haploid.dx.kvs.KVSTypeconverter
import com.ibm.haploid.dx.kvs.SelectDocuVersByAttributesInput
import com.ibm.haploid.dx.kvs.SelectTeilByKeyInput
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.kvs.utils.MaxDVNummer
import eu.man.phevos.dx.kvs.utils.StringUtils
import eu.man.phevos.dx.util.interfaces.MTBPartMetadata
import eu.man.phevos.dx.kvs.utils.ResponsibleUser
import eu.man.phevos.dx.util.interfaces.PartInfo
import eu.man.phevos.dx.util.interfaces.PDA.CPL

case class KVSFilenameForCPLInput(partInfo: PartInfo)

class GetKVSFilenameForCPLImpl

  extends Service[KVSFilenameForCPLInput, String]

  with KVSTypeconverter

  with MaxDVNummer

  with StringUtils

  with ResponsibleUser {

  def doService(in: KVSFilenameForCPLInput): Result[String] = {
    lazy val docuversions = getDocuversionsByKey(CPL.value + ":" + in.partInfo.vwPartNumber, in.partInfo)

    lazy val dvNum = getMaxDVNummer(docuversions)

    lazy val length = if (in.partInfo.vwPartNumberShort.startsWith("TEST")) 72 else 70

    KVSServices.CheckPartNumberExists(CheckPartNumberExistsInput(in.partInfo)) match {
      case Success(_) ⇒
        Success(
          replaceSpecialChars(mkLength(mkLength(in.partInfo.vwPartNumber, "_", 14)
            + "_RAW_CPL_"
            + mkLength(dvNum.toString(), "0", 3, false)
            + "_____PARAMETRIC_FILES_"
            + in.partInfo.mtbPartNumber
            + "_"
            + in.partInfo.mtbPartIndex, "_", length)) + ".zip")
    }
  }

}