package eu.man.phevos

package dx

package kvs

package services

import scala.collection.JavaConversions.asScalaBuffer

import com.ibm.haploid.core.service.{ Success, Result }
import com.ibm.haploid.core.service.Service
import com.ibm.haploid.dx.kvs.{ SelectZuordnungByAttributesInput, KVSBaseServices }
import com.typesafe.config.Config

import eu.man.phevos.dx.kvs.utils.{ StringUtils, ResponsibleUser, KStandUtils }
import eu.man.phevos.dx.kvs.KVSException
import eu.man.phevos.dx.util.interfaces.PartInfo

case class UpdateKStandInput(partinfo: PartInfo)

class UpdateKStandImpl

  extends Service[UpdateKStandInput, Boolean]

  with ResponsibleUser

  with StringUtils

  with KStandUtils {

  def doService(in: UpdateKStandInput): Result[Boolean] = {
    lazy val kStand = getKStandForPartWithIndex(in.partinfo)

    lazy val docVers = KVSBaseServices.SelectZuordnung({
      val map = Map(
        "TEIL_KEY" -> in.partinfo.vwPartNumber,
        "KSTAND_KSTAND" -> kStand.getString("KSTAND_KSTAND"))

      SelectZuordnungByAttributesInput(map, user(in.partinfo))
    }) match {
      case Success(c) if (c.hasPath("ZUORDNUNG")) ⇒
        c.getConfigList("ZUORDNUNG").toList.foldRight[Option[Config]](None) { (e, selected) ⇒
          selected match {
            case None ⇒
              Some(e)
            case Some(config) ⇒
              if (e.getLong("DV_DATUM") > config.getLong("DV_DATUM"))
                Some(e)
              else
                Some(config)
          }
        }.get
      case Success(c) ⇒
        throw KVSException("Zuordnung not found (" + c + ")")
    }

    val kStandDescription = try {
      kStand.getString("KSTAND_BESCHREIBUNG")
    } catch {
      case e: Throwable ⇒ ""
    }

    val kStandDrawingDate = try {
      getTimestampFromString(kStand.getString("KSTAND_ZEICHNUNGSDATUM"))
    } catch {
      case e: Throwable ⇒ 0L
    }

    val newKStandDescription = in.partinfo.vwChangeNumber match {
      case None ⇒
        val dvDesc = docVers.getString("DV_BESCHREIBUNG").split(" ")
        in.partinfo.mtbPartIndex + " " + in.partinfo.mtbChangeNumber + " " + dvDesc(dvDesc.size - 1)
      case Some(s) ⇒
        in.partinfo.mtbPartIndex + " " + in.partinfo.mtbChangeNumber + " " + s
    }

    val newKStandDrawingDate = in.partinfo.vwDrawingDate match {
      case None ⇒
        getTimestampFromString(docVers.getString("DV_DATUM"))
      case Some(l) ⇒
        l
    }

    if (!kStandDescription.equals(newKStandDescription) ||
      kStandDrawingDate != newKStandDrawingDate) {

      val map = Map() ++
        {
          if (!kStandDescription.equals(newKStandDescription))
            Map("KSTAND_BESCHREIBUNG" -> newKStandDescription)
          else
            Map()
        } ++
        {
          if (kStandDrawingDate != newKStandDrawingDate) {
            Map("KSTAND_ZEICHNUNGSDATUM" -> getDateStringFromLong(Some(newKStandDrawingDate), "YYYYMMddHHmmss"))
          } else
            Map()
        }

      val id = kStand.getString("KSTAND_ID")

      KVSBaseServices.UpdateKStand(com.ibm.haploid.dx.kvs.UpdateKStandInput(id, map, user(in.partinfo))) match {
        case Success(c) if c.hasPath("KSTAND") ⇒
          Success(true)
        case Success(c) ⇒
          throw KVSException("Failure while updating KStand (" + c + ")")
      }
    } else Success(true)
  }

}