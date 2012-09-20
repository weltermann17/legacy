package eu.man.phevos.dx.kvs.services
import java.nio.file.Path
import com.ibm.haploid.core.service.Result
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.service.Service
import com.ibm.haploid.dx.kvs.KVSBaseServices
import com.ibm.haploid.dx.kvs.SelectDocuVersByAttributesInput
import com.ibm.haploid.core.service.Failure
import eu.man.phevos.dx.util.interfaces.MTBPartMetadata
import eu.man.phevos.dx.util.interfaces.ReleaseLevel
import eu.man.phevos.dx.kvs.utils.ResponsibleUser
import eu.man.phevos.dx.util.interfaces.PartInfo
import eu.man.phevos.dx.util.interfaces.EZISMetadata

import com.ibm.haploid.core.logger

case class CheckDVForEZISFileExistsInput(partInfo: PartInfo, metadata: EZISMetadata)

class CheckDVForEZISFileExistsImpl

  extends Service[CheckDVForEZISFileExistsInput, Boolean]

  with ResponsibleUser {

  def doService(in: CheckDVForEZISFileExistsInput): Result[Boolean] = {
    val search = ""
      .concat(in.metadata.index)
      .concat(" ")
      .concat(in.metadata.release.value)
      .concat("*")

    val key = in.partInfo.vwPartNumber + ",Blatt=" + in.metadata.sheet.toString

    val map = Map("DV_KEY" -> key, "DV_BESCHREIBUNG" -> search)

    KVSBaseServices.SelectDocuVers(SelectDocuVersByAttributesInput(map, user(in.partInfo))) match {
      case Success(s) ⇒
        if (s.hasPath("DOCUVERS")){
          Success(true)}
        else
        {
          Success(false)
          }
    }
  }

}