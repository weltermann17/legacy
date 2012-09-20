package eu.man.phevos

package dx

package kvs

package services

import com.ibm.haploid.core.service.Result
import com.ibm.haploid.core.service.Service
import com.typesafe.config.Config

import eu.man.phevos.dx.kvs.utils.{ StringUtils, ResponsibleUser, CheckDVExists, AttachDVToKStand }
import eu.man.phevos.dx.kvs.KVSException
import eu.man.phevos.dx.util.interfaces.{ PartInfo, MTBPartIndexOrdering, EZISMetadata }

case class AttachDVOfHighestIndexOfSheetToKStandInput(partinfo: PartInfo, metadata: EZISMetadata)

class AttachDVOfHighestIndexOfSheetToKStandImpl

  extends Service[AttachDVOfHighestIndexOfSheetToKStandInput, Boolean]

  with AttachDVToKStand

  with StringUtils

  with ResponsibleUser

  with CheckDVExists {

  def doService(in: AttachDVOfHighestIndexOfSheetToKStandInput): Result[Boolean] = {

    val docuVers = findAllTZELZKAB(in.partinfo, in.metadata)

    if (docuVers.size == 0)
      throw KVSException("No document versions found.")

    val selected = docuVers.foldLeft[Option[Config]](None) {
      case (None, current) ⇒
        Some(current)
      case (Some(highest), current) ⇒
        val hIndex = highest.getString("DV_BESCHREIBUNG").split(" ")(0)
        val cIndex = current.getString("DV_BESCHREIBUNG").split(" ")(0)

        if (MTBPartIndexOrdering(hIndex) > cIndex)
          Some(highest)
        else if (MTBPartIndexOrdering(hIndex) < cIndex)
          Some(current)
        else { // == 
          val hRel = highest.getString("DV_BESCHREIBUNG").split(" ")(1)

          if (hRel.equals("kn"))
            Some(highest)
          else
            Some(current)
        }
    }.get

    attachDVToKStand(selected, in.partinfo, in.metadata)
  }

}