package eu.man.phevos

package dx

package kvs

package services

import com.ibm.haploid.core.service.{ Success, Result, Failure }
import com.ibm.haploid.core.service.Service
import com.ibm.haploid.dx.kvs.{ SelectKStandByAttributesInput, KVSBaseServices }

import eu.man.phevos.dx.kvs.utils.{ StringUtils, ResponsibleUser }
import eu.man.phevos.dx.kvs.KVSException
import eu.man.phevos.dx.util.interfaces.PDA.{ TZ, TM, KAB, ELZ, CPL }
import eu.man.phevos.dx.util.interfaces.{ PartInfo, MTBPartFile }

case class UploadFileInput(partInfo: PartInfo, file: MTBPartFile)

class UploadFileImpl

  extends Service[UploadFileInput, Boolean]

  with StringUtils

  with ResponsibleUser {

  def doService(in: UploadFileInput): Result[Boolean] = {

    lazy val pda = in.file.metadata.pda

    lazy val vwChangeNumber = in.partInfo.vwChangeNumber match {
      case Some(s) ⇒
        s
      case None ⇒
        defaultVWChangeNumber
    }

    lazy val dvDesc = {
      pda match {
        case CPL | TM ⇒
          in.file.metadata.mtbPartIndex + " " + vwChangeNumber
        case TZ | KAB | ELZ ⇒
          in.file.metadata.mtbPartIndex + " " + in.file.metadata.releaseLevel.value + " " + vwChangeNumber
      }
    }

    lazy val kStandExists = {
      val map = Map("KSTAND_KEY" -> in.partInfo.vwPartNumber,
        "KSTAND_OBJEKTTYP" -> "T",
        "KSTAND_BESCHREIBUNG" -> in.partInfo.mtbPartIndex.concat("*"))

      KVSBaseServices.SelectKStand(SelectKStandByAttributesInput(map, user(in.partInfo))) match {
        case Success(s) ⇒
          if (s.hasPath("KSTAND")) {
            Some(s.getConfigList("KSTAND").get(0))
          } else None
        case Failure(e) ⇒
          None
      }
    }

    val map = Map(
      "TRANSPARENT_MODE" -> "1",
      "DOCU_KEY" -> {
        pda.value + ":" + in.partInfo.vwPartNumber + {
          pda match {
            case (TZ | ELZ | KAB) ⇒
              ",Blatt=" + in.file.metadata.page // Another method ...
            case (CPL) ⇒
              ",Container=1"
            case _ ⇒
              ""
          }
        }
      },
      "DV_NUMMER" -> "next",
      "DV_STATUS" -> "I",
      "DV_BESCHREIBUNG" -> dvDesc,
      "DV_PF_NACHNAME" -> "Project Phevos",
      "DV_PF_VORNAME" -> "R&D",
      "DV_SPERRSTATUS" -> "S",
      "KSTAND_OBJEKTTYP.0" -> "T",
      "KSTAND_KSTAND.0" -> {
        in.partInfo.vwKStand match {
          case None ⇒
            kStandExists match {
              case None ⇒
                "Knext"
              case Some(config) ⇒
                "K" + config.getString("KSTAND_NUMMER")
            }
          case Some(s) ⇒
            s
        }

      },
      "KSTAND_BESCHREIBUNG.0" -> (in.partInfo.mtbPartIndex + " " + in.partInfo.mtbChangeNumber + " "  + vwChangeNumber),
      "TEIL_KEY.0" -> in.partInfo.vwPartNumber,
      "KSTAND_ROWS" -> "1") ++ {
        pda match {
          case (TZ | ELZ | KAB) ⇒
            Map(
              "DV_DATUM" -> getDateStringFromLong(in.partInfo.vwDrawingDate, "YYYYMMddHHmmss"),
              "KSTAND_MODDATUM.0" -> getDateStringFromLong(in.partInfo.vwDrawingDate, "ddMMYYYY"),
              "KSTAND_ZEICHNUNGSDATUM.0" -> getDateStringFromLong(in.partInfo.vwDrawingDate, "YYYYMMddHHmmss"))
          case _ ⇒
            Map()
        }
      }

    KVSBaseServices.UploadFile(com.ibm.haploid.dx.kvs.UploadFileInput(in.file.path, responsible = user(in.partInfo), api2attributes = map)) match {
      case Success(s) if s.hasPath("DATA") ⇒
        Success(true)
      case Success(s) ⇒
        Failure(KVSException("Couldn't upload file " + in.file + "; KVS-Result: " + s))
    }
  }

}