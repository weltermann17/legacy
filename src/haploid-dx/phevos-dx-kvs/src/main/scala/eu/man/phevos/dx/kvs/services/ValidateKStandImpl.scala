package eu.man.phevos

package dx

package kvs

package services

import java.sql.Timestamp

import com.ibm.haploid.core.newLogger
import com.ibm.haploid.core.service.{ Success, Result }
import com.ibm.haploid.core.service.Service
import com.ibm.haploid.dx.kvs.KVSObject.TYP_T
import com.ibm.haploid.dx.kvs.{ SelectKStandByKeyInput, KVSBaseServices }

import eu.man.phevos.dx.kvs.utils.{ StringUtils, ResponsibleUser }
import eu.man.phevos.dx.util.interfaces.PartInfo

case class ValidateKStandInput(partInfo: PartInfo)

class ValidateKStandImpl

  extends Service[ValidateKStandInput, Boolean]

  with StringUtils

  with ResponsibleUser {

  val logger = newLogger(this)

  def doService(in: ValidateKStandInput): Result[Boolean] = {
    if (in.partInfo.vwKStand == None) {
      Success(true)
    } else {
      lazy val key = in.partInfo.vwPartNumber + ":" + in.partInfo.vwKStand.get

      KVSBaseServices.SelectKStand(SelectKStandByKeyInput(key, TYP_T, user(in.partInfo))) match {
        case Success(s) ⇒
          if (!s.hasPath("KSTAND")) {
            logger.debug("KSTAND not available for " + key + ".")
            return Success(false)
          }

          if (s.getConfigList("KSTAND").size() != 1) {
            logger.debug("Unambiguous KSTAND found for " + key + ".")
            return Success(false)
          }

          val kstand = s.getConfigList("KSTAND").get(0)

          val kstandDesc = kstand.getString("KSTAND_BESCHREIBUNG")
          if (!kstandDesc.contains(in.partInfo.mtbPartIndex)) {
            logger.debug("KStand description doesn't contain CRT MTB Part Index.")
            return Success(false)
          }

          if (in.partInfo.vwDrawingDate != None) {
            val kstandDrawingDate = kstand.getString("KSTAND_ZEICHNUNGSDATUM")

            if (!kstandDrawingDate.trim().equals(getDateString(Some(new Timestamp(in.partInfo.vwDrawingDate.get)), "YYYYMMddHHmmss"))) {
              logger.debug("KSTAND_DATUM doesn't equal date from CRT.")
              return Success(false)
            }
          }

          Success(true)
      }
    }
  }

}