package eu.man.phevos

package dx

package kvs

package services

import com.ibm.haploid.core.service.{ Success, Result }
import com.ibm.haploid.core.service.Service
import com.ibm.haploid.dx.kvs.{ SelectDocuVersByAttributesInput, KVSBaseServices }

import eu.man.phevos.dx.kvs.utils.{ StringUtils, ResponsibleUser, AttachDVToKStand }
import eu.man.phevos.dx.kvs.KVSException
import eu.man.phevos.dx.util.interfaces.{ PartInfo, EZISMetadata }

case class AttachPageDVToKStandInput(partinfo: PartInfo, metadata: EZISMetadata)

class AttachPageDVToKStandImpl

  extends Service[AttachPageDVToKStandInput, Boolean]

  with AttachDVToKStand

  with ResponsibleUser

  with StringUtils {

  def doService(in: AttachPageDVToKStandInput): Result[Boolean] = {
    val search = ""
      .concat(in.metadata.index)
      .concat(" ")
      .concat(in.metadata.release.value)
      .concat("*")

    val key = in.partinfo.vwDefiningIdent + ",Blatt=" + in.metadata.sheet.toString

    val map = Map("DV_KEY" -> key, "DV_BESCHREIBUNG" -> search)

    KVSBaseServices.SelectDocuVers(SelectDocuVersByAttributesInput(map, user(in.partinfo))) match {
      case Success(s) if (s.hasPath("DOCUVERS")) ⇒
        val dv = s.getConfigList("DOCUVERS").get(0)
        attachDVToKStand(dv, in.partinfo, in.metadata)
      case Success(s) ⇒
        throw KVSException("Couldn't select docuversion.")
    }
  }

}