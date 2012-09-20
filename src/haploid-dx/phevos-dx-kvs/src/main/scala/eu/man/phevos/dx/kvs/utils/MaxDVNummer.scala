package eu.man.phevos.dx.kvs.utils
import scala.collection.JavaConversions.asScalaBuffer

import com.ibm.haploid.core.service.{ Success, Failure }
import com.ibm.haploid.dx.kvs.{ SelectDocuVersByKeyInput, SelectDocuVersByAttributesInput, KVSTypeconverter, KVSBaseServices }
import com.typesafe.config.Config

import eu.man.phevos.dx.util.interfaces.PartInfo

trait MaxDVNummer { self: KVSTypeconverter with ResponsibleUser ⇒

  def getDocuversionsByTeilKey(partInfo: PartInfo) = {
    KVSBaseServices.SelectDocuVers(SelectDocuVersByAttributesInput("TEIL_KEY" -> partInfo.vwPartNumber, user(partInfo))) match {
      case Success(result) ⇒
        if (result.hasPath("DOCUVERS")) {
          Some(result.getConfigList("DOCUVERS").toList)
        } else {
          None
        }
      case Failure(e) ⇒
        None
    }
  }

  def getDocuversionsByKey(dvKey: String, partInfo: PartInfo) = {
    KVSBaseServices.SelectDocuVers(SelectDocuVersByKeyInput(dvKey, user(partInfo))) match {
      case Success(result) ⇒
        if (result.hasPath("DOCUVERS")) {
          Some(result.getConfigList("DOCUVERS").toList)
        } else {
          None
        }
      case Failure(e) ⇒
        None
    }
  }

  def getMaxDVNummer(docuversions: Option[List[Config]]): Int = {
    if (docuversions == None) {
      1
    } else {
      1 + docuversions.get.foldLeft(0) { (max, config) ⇒
        val dvNum = config.getInt("DV_NUMMER")

        if (dvNum > max)
          dvNum
        else
          max
      }
    }
  }

}