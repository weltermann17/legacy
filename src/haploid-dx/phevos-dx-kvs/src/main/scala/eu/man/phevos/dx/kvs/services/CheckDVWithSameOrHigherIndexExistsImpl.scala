package eu.man.phevos.dx.kvs.services
import scala.collection.JavaConversions._
import com.ibm.haploid.core.service.{ Service, Success, Result }
import com.ibm.haploid.dx.kvs.{ SelectDocuVersByKeyInput, KVSBaseServices }
import com.typesafe.config.Config
import eu.man.phevos.dx.kvs.utils.ResponsibleUser
import eu.man.phevos.dx.util.interfaces.{ PartInfo, MTBPartMetadata }
import eu.man.phevos.dx.util.interfaces.MTBPartIndexOrdering
import eu.man.phevos.dx.kvs.utils.CheckDVExists
import eu.man.phevos.dx.util.interfaces.EZISSheetMetadata

import com.ibm.haploid.core.logger

case class CheckDVWithSameOrHigherIndexExistsInput(partinfo: PartInfo, metadata: EZISSheetMetadata)

class CheckDVWithSameOrHigherIndexExistsImpl

  extends Service[CheckDVWithSameOrHigherIndexExistsInput, (Boolean, String)]

  with ResponsibleUser

  with CheckDVExists {

  def doService(in: CheckDVWithSameOrHigherIndexExistsInput): Result[(Boolean, String)] = {

    lazy val docVersions = findAllTZELZKAB(in.partinfo, in.metadata)

    if (docVersions.size == 0)
      Success(false, "")
    else {

      var key = ""

      docVersions.find { dv ⇒
        val dvMTBPartIndex = dv.getString("DV_BESCHREIBUNG").split(" ")(0)
        key = in.partinfo.mtbPartNumber + " " + String.format("%1$4s", dv.getString("DOCU_STR_NR")).replace(" ", "0") + " " + dvMTBPartIndex

        if (MTBPartIndexOrdering(in.metadata.index) <= dvMTBPartIndex) {
          true
        } else {
          false
        }
      } match {
        case None ⇒
          Success(false, key)
        case _ ⇒
          Success(true, key)
      }
    }
  }

}