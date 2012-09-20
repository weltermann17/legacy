package eu.man.phevos.dx.kvs.utils
import scala.collection.JavaConversions.asScalaBuffer
import com.ibm.haploid.core.service.{ Success, Result, Failure }
import com.ibm.haploid.dx.kvs.{ SelectDocuVersByKeyInput, SelectDocuVersByAttributesInput, KVSBaseServices }
import com.typesafe.config.Config
import eu.man.phevos.dx.util.interfaces.{ PartInfo, PDA, MTBPartMetadata }
import eu.man.phevos.dx.util.interfaces.PDA
import eu.man.phevos.dx.util.interfaces.EZISMetadata
import eu.man.phevos.dx.util.interfaces.PartInfo

trait CheckDVExists { self: ResponsibleUser ⇒

  def check(partinfo: PartInfo, pda: PDA): Result[Boolean] = {
    val key = pda.value + ":".concat(partinfo.vwPartNumber)
    val search = partinfo.mtbPartIndex + "*"

    val map = Map("DV_KEY" -> key, "DV_BESCHREIBUNG" -> search)

    KVSBaseServices.SelectDocuVers(SelectDocuVersByAttributesInput(map, user(partinfo))) match {
      case Success(s) ⇒
        if (s.hasPath("DOCUVERS") && s.getConfigList("DOCUVERS").size() > 0)
          Success(true)
        else
          Success(false)
      case Failure(e) ⇒
        Failure(e)
    }
  }

  def findAllTZELZKAB(partinfo: PartInfo, md: EZISMetadata) = {
    val key = partinfo.vwDefiningIdent + ",Blatt=" + md.sheet

    KVSBaseServices.SelectDocuVers(SelectDocuVersByKeyInput(key, user(partinfo))) match {
      case Success(c) if (c.hasPath("DOCUVERS")) ⇒
        c.getConfigList("DOCUVERS").toList
      case Success(_) ⇒
        List[Config]()
    }
  }

}