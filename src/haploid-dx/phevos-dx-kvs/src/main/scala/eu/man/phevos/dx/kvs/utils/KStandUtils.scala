package eu.man.phevos

package dx

package kvs

package utils

import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.kvs.KVSObject.TYP_T
import com.ibm.haploid.dx.kvs.{ SelectKStandByKeyInput, SelectKStandByAttributesInput, KVSBaseServices }

import eu.man.phevos.dx.kvs.KVSException
import eu.man.phevos.dx.util.interfaces.PartInfo

trait KStandUtils { self: ResponsibleUser ⇒

  def getKStandForPartWithIndex(partinfo: PartInfo) = {
    KVSBaseServices.SelectKStand(partinfo.vwKStand match {
      case None ⇒
        val map = Map(
          "TEIL_KEY" -> partinfo.vwPartNumber,
          "KSTAND_OBJEKTTYP" -> "T",
          "KSTAND_BESCHREIBUNG" -> (partinfo.mtbPartIndex + "*"))

        SelectKStandByAttributesInput(map, user(partinfo))
      case Some(kstand) ⇒
        SelectKStandByKeyInput(partinfo.vwPartNumber + ":" + kstand, TYP_T, user(partinfo))
    }) match {
      case Success(c) if (c.hasPath("KSTAND")) ⇒
        c.getConfigList("KSTAND").get(0)
      case Success(c) ⇒
        throw new KStandNotFoundException(c.toString)
    }
  }

}