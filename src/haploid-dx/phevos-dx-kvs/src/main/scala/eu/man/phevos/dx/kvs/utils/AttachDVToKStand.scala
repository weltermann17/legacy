package eu.man.phevos.dx.kvs

package utils

import eu.man.phevos.dx.util.interfaces.PartInfo
import com.typesafe.config.Config
import com.ibm.haploid.dx.kvs.KVSBaseServices
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.service.Result
import com.ibm.haploid.dx.kvs.LinkDVToKStandInput
import com.ibm.haploid.dx.kvs.SelectKStandByKeyInput
import com.ibm.haploid.dx.kvs.KVSObject.TYP_T
import eu.man.phevos.dx.util.interfaces.MTBPartMetadata
import com.ibm.haploid.dx.kvs.CreateKStandForDVInput
import com.ibm.haploid.dx.kvs.SelectKStandByAttributesInput
import eu.man.phevos.dx.util.interfaces.EZISMetadata

trait AttachDVToKStand { self: ResponsibleUser with StringUtils ⇒

  def attachDVToKStand(dv: Config, partinfo: PartInfo, metadata: EZISMetadata): Result[Boolean] = {
    val dvId = dv.getString("DV_ID")

    partinfo.vwKStand match {
      case None ⇒
        val desc = partinfo.mtbPartIndex + "*"
        val map = Map(
          "TEIL_KEY" -> partinfo.vwPartNumber,
          "KSTAND_BESCHREIBUNG" -> desc)

        KVSBaseServices.SelectKStand(SelectKStandByAttributesInput(map, user(partinfo))) match {
          case Success(s) if (s.hasPath("KSTAND")) ⇒
            val kStand = s.getConfigList("KSTAND").get(0).getString("KSTAND_KSTAND")
            attachToKStand(partinfo, kStand, dvId)
          case _ ⇒
            val map = Map(
              "OBJEKTTYP" -> "T",
              "MODDATUM" -> getDateStringFromLong(partinfo.vwDrawingDate, "ddMMYYYY"),
              "DATUM" -> getDateStringFromLong(partinfo.vwDrawingDate, "YYYYMMddHHmmss"),
              "BESCHREIBUNG" -> (partinfo.mtbPartIndex + " " + (partinfo.vwChangeNumber match {
                case Some(s) ⇒
                  s
                case None ⇒
                  defaultVWChangeNumber
              })))

            KVSBaseServices.CreateKStandForDV(CreateKStandForDVInput(dvId, partinfo.vwPartNumber, map, user(partinfo))) match {
              case Success(r) ⇒
                // TODO check result
                Success(true)
            }
        }

      case Some(kstand) ⇒
        attachToKStand(partinfo, partinfo.vwKStand.get, dvId)
    }

  }

  private def attachToKStand(partinfo: PartInfo, kStand: String, dvId: String): Result[Boolean] = {
    KVSBaseServices.SelectKStand(SelectKStandByKeyInput(partinfo.vwPartNumber + ":" + kStand, TYP_T, user(partinfo))) match {
      case Success(c) ⇒
        val kStandId = c.getConfigList("KSTAND").get(0).getString("KSTAND_ID")
        KVSBaseServices.LinkDVToKStand(LinkDVToKStandInput(dvId, kStandId, user(partinfo))) match {
          case Success(r) ⇒
            // TODO check result
            Success(true)
        }
    }
  }

}