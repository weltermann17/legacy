package eu.man.phevos.dx.kvs.services
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Timestamp
import com.ibm.haploid.core.service.Failure
import com.ibm.haploid.core.service.Result
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.service.Service
import com.ibm.haploid.dx.kvs.KVSTypeconverter
import eu.man.phevos.dx.kvs.utils.MaxDVNummer
import eu.man.phevos.dx.kvs.utils.StringUtils
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.util.interfaces.MTBPartMetadata
import eu.man.phevos.dx.util.interfaces.PartInfo
import eu.man.phevos.dx.kvs.utils.ResponsibleUser

case class GetKVSFilenameForTIFFInput(partInfo: PartInfo, md: MTBPartMetadata)

class GetKVSFilenameForTIFFImpl

  extends Service[GetKVSFilenameForTIFFInput, String]

  with KVSTypeconverter

  with MaxDVNummer

  with StringUtils

  with ResponsibleUser {

  def doService(in: GetKVSFilenameForTIFFInput): Result[String] = {
    KVSServices.CheckPartNumberExists(CheckPartNumberExistsInput(in.partInfo)) match {
      case Success(_) ⇒
        lazy val pda = in.md.pda

        lazy val docuversions = getDocuversionsByKey(pda.value + ":" + in.partInfo.vwPartNumber + ",Blatt=" + in.md.page, in.partInfo)

        lazy val dvNum = getMaxDVNummer(docuversions)

        lazy val length = if (in.partInfo.vwPartNumberShort.startsWith("TEST")) 72 else 70

        val result = replaceSpecialChars(mkLength(mkLength(in.partInfo.vwPartNumber, "_", 14)
          + "_DRW_"
          + mkLength(pda.value, "_", 3)
          + "_"
          + mkLength(dvNum.toString, "0", 3, false)
          + "_____BLATT_"
          + mkLength(in.md.page.toString, "_", 3)
          + "_"
          + getDateStringFromLong(in.partInfo.vwDrawingDate)
          + "_"
          + in.md.mtbPartNumber
          + "_"
          + in.md.mtbPartIndex
          + "_"
          + mkLength(in.md.releaseLevel.value.toUpperCase, "_", 2), "_", length)) + ".tif"

        Success(result)
      case Failure(e) ⇒
        Failure(e)
    }
  }

}