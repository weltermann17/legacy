package eu.man

package phevos

package dx

package crt

import com.ibm.haploid.core.service._
import java.sql.Timestamp
import util.interfaces.PartInfo
import util.interfaces.EZISSheetMetadata

object CrtServices {

  class CrtInvalidPartStatusException(s: String) 
  
  extends Exception(s) 
  
  with ServiceException

  case class EntitledParts(partinfos: List[PartInfo])

  /**
   * Identifier - part identifier consisting of the MTB part version, MTB part revision, partner partnumber and partner revision
   * if not stated differently, partnerpartnumber and partnerrevision are not used
   */
  case class Identifier(partversion: String, revision: String, partnerpartnumber: String, partnerrevision: Option[String]) {
    val shortpartversion = partversion.replace(".", "").replace("-", "")
    val shortrevision = revision.replace("_", " ")
    val shortpartnerpartnumber = partnerpartnumber.replace(".", "")
  }

  /**
   * Extended Identifier - like Identifier +  DX_STATUS, KSTAND and DRAWING_DATE
   *
   */
  case class Service2Identifier(partversion: String, revision: String, partnerpartnumber: String, dxcompleted: Boolean, partnerrevision: String, drawingdate: String) {
    val shortpartversion = partversion.replace(".", "").replace("-", "")
    val shortrevision = revision.replace("_", " ")
    val shortpartnerpartnumber = partnerpartnumber.replace(".", "")
  }

  case class PartnerIdentifier(partnerpartnumber: String,
    partnerrevision: String,
    drawingdate: String,
    dxstatus: String,
    titleblockexists: Boolean)

  case class CurrentDxStatus(status: String)

  case class CurrentDrawingDate(date: String)

  case class TitleBlockFlag(valid: Boolean)

  case class Sheets(sheets: List[EZISSheetMetadata])

  object EntitledDxStatus extends Service[Unit, EntitledParts] {
    def doService(u: Unit): Result[EntitledParts] = Success(CrtUnload.getJobList)
  }

  object DxStatus extends Service[Identifier, CurrentDxStatus] {
    def doService(ident: Identifier): Result[CurrentDxStatus] = Success(CrtTable.getDxStatus(ident))
  }

  object DrawingDate extends Service[Identifier, CurrentDrawingDate] {
    def doService(ident: Identifier): Result[CurrentDrawingDate] = Success(CrtTable.getDrawingDate(ident))
  }

  object TitleBlock extends Service[Identifier, TitleBlockFlag] {
    def doService(ident: Identifier): Result[TitleBlockFlag] = Success(CrtTable.getTitleblock(ident))
  }

  object PartnerIdent extends Service[Identifier, PartnerIdentifier] {
    def doService(ident: Identifier): Result[PartnerIdentifier] = Success(CrtTable.getPartnerData(ident))
  }

  /**
   * SetDxStatusToWork - set the dx_status to 05 ("in Arbeit")
   *
   * input:
   * - Identifier with the partversion and revision
   *
   * preconditions:
   * - dx_status in 03 or 04
   *
   * results:
   * - dx_status is set to 05 "in Arbeit"
   *
   * output:
   * - the new dx_status
   */

  object SetDxStatusToWork extends Service[Identifier, CurrentDxStatus] {
    def doService(ident: Identifier): Result[CurrentDxStatus] = Success(CrtTable.setDxStatus(ident))
  }

  /**
   * SetDrawingDate - updates the drawing date
   *
   * input:
   * - Identifier with the partversion and revision
   *
   * preconditions:
   * - dx_status is not XX ("DX nicht erforderlich")
   * - current drawing_date is empty
   *
   * results:
   * - dx_status is set to XX "DX nicht geprüft"
   * - drawing_date is set to current date
   *
   * output:
   * - the new drawing_date
   */

  //  object SetDrawingDate extends Service[Identifier, CurrentDrawingDate] {
  //    def doService(ident : Identifier) : Result[CurrentDrawingDate] = Success(CrtTable.setDrawingdate(ident))
  //  }

  /**
   * SetKstandDrawingDateDxStatus - updates partner revision, drawing date and dx_status
   *
   * input:
   * - Identifier with the partversion, revision, partner partnumber, partner revision, drawing date and boolean value, if dxstatus should be set to completed
   *
   *
   * preconditions:
   * - current drawing_date is empty
   * - current dx_status is 05 ("in Arbeit")
   * - following 3 sets of parameters are allowed
   * 	drawing date with value, partner revision empty and dxcompleted = false
   * 	drawing date with value, partner revision with value and dxcompleted = false
   * 	drawing date with value, partner revision with value and dxcompleted = true
   *
   * results:
   * - drawing_date is set to current date
   * - partner revision is set to given value (if passed)
   * - dx_status is set to 06 ("in abgeschlossen") (if dxcompleted = true)
   *
   * output:
   * - none
   */

  object SetKstandDrawingDateDxStatus extends Service[Service2Identifier, Unit] {
    def doService(ident: Service2Identifier): Result[Unit] = Success(CrtTable.setPartnerDatas(ident))
  }

  /**
   * ValidSheets - returns a List[String] with the page numbers of all valid sheets
   *
   * input:
   * - Identifier with the partversion and revision
   *
   * preconditions:
   * - none
   *
   * output:
   * - list of type String of the page numbers of all valid sheets
   */

  object ValidSheets extends Service[Identifier, Sheets] {
    def doService(ident: Identifier): Result[Sheets] = Success(SadisTable.getValidSheets(ident))
  }

}