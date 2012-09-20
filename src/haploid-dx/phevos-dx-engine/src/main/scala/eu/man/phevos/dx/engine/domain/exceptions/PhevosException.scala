package eu.man.phevos.dx.engine.domain.exceptions

import com.ibm.haploid.core.service.ServiceException

sealed abstract class PhevosException(

  val errNo: String,

  val message: String)

  extends Exception(message)

  with ServiceException {

  override def getMessage = "[" + errNo + "] " + message

}

object PhevosException {

  def unapply(e: PhevosException): Option[(String, String)] = {
    e match {
      case e if e.isInstanceOf[PhevosException] ⇒
        Some((e.asInstanceOf[PhevosException].errNo, e.asInstanceOf[PhevosException].message))
      case _ ⇒
        None
    }
  }

}

/**
 *
 */
case class P0A1_UnexpectedResult(e: Throwable) extends PhevosException("P0A-1", "Engine exception during CRT partinfo checks (" + e.getMessage + ")")

case class P0A2_UnexpectedResult(e: Throwable) extends PhevosException("P0A-2", "Engine exception during job execution (" + e.getMessage + ")")

/**
 *
 */
case object P0B1_StandardPart extends PhevosException("P0B-1", "Part is standard part - perform manual data exchange")

case object P0B2_PartnumberDoesNotExist extends PhevosException("P0B-2", "Part number does not exist in KVS")

case object P0B3_KStandNotValid extends PhevosException("P0B-3", "Entries in CRT do not match values at KVS K-Stand")

case object P0B4_Superceded extends PhevosException("P0B-4", "MTB index is superseded in KVS")

case object P0B5_EEPart extends PhevosException("P0B-5", "EE data. Please import using manual process.")

/**
 *
 */
case object P0C1_KStand extends PhevosException("P0C-1", "Px processes completed successfully, but KStand doesn't contain drawing date.")

case class P0C2_DrawingDate(e: Throwable) extends PhevosException("P0C-2", "KStand doesn't contain drawing date after Error (" + e.getMessage + ")")

case class P0C3_CRTUpdateAfterFailure(e: Throwable) extends PhevosException("P0C-3", "CRT update failed after Failure (" + e.getMessage + ")")

case object P0C4_CRTUpdateAfterSuccess extends PhevosException("P0C-4", "CRT update failed after successful job.")

/**
 *
 */
case object P1A1_PartIndexNotFound extends PhevosException("P1A-1", "Part index could not be found in LCA")

case object P1A2_PartIndexNotReleased extends PhevosException("P1A-2", "Part index not released in LCA")

/**
 * 
 */
case object P1B1_NoEZISFiles extends PhevosException("P1B1", "No EZIS files available for part index.")

/**
 *
 */
case object P1C1_PartIndexNotFound extends PhevosException("P1C-1", "Part index could not be found in LCA")

case object P1C2_PartIndexNotReleased extends PhevosException("P1C-2", "Part index not released in LCA")

case object P1C3_NoCATPartFound extends PhevosException("P1C-3", "No CATPart found in LCA at single part")

/**
 *
 */
case object P2A1_RenamingError extends PhevosException("P2A-1", "Error renaming CPL container file (could not get filename from KVS)")

/**
 *
 */
case object P2B1_KNRelease extends PhevosException("P2B-1", "EZIS files of NEW or MOD part have kn release - perform manual data exchange")

case object P2B2_PDF extends PhevosException("P2B-2", "PDF file requires foreign part header – perform manual data exchange")

/**
 *
 */
case object P3A1_CPLDoesNotExist extends PhevosException("P3A-1", "CPL file does not exist in working directory")

case object P3A2_PartNotExists extends PhevosException("P3A-2", "Part number does not exist in KVS")

case object P3A3_KStandInvalid extends PhevosException("P3A-3", "Entries in the CRT do not match K-Stand values")

case object P3A4_CPLAlreadyExists extends PhevosException("P3A-4", "CPL version already exists")

case object P3A5_UploadError extends PhevosException("P3A-5", "Error uploading CPL file")

/**
 *
 */
case object P3B1_PartNotExists extends PhevosException("P3B-1", "Part number does not exist in KVS")

case object P3B2_KStandInvalid extends PhevosException("P3B-2", "Entries in CRT do not match values at KVS K-Stand")

case object P3B3_KStandUpdateFailed extends PhevosException("P3B-3", "Updating of K-Stand attributes failed/ Upload of sheet failed")

/**
 *
 */
case object P3C1_TMTarNotExists extends PhevosException("P3C-1", "TM tar container does not exist in working directory")

case object P3C2_PartNotExists extends PhevosException("P3C-2", "Part number does not exist in KVS")

case object P3C3_KStandInvalid extends PhevosException("P3C-3", "Entries in CRT do not match values at KVS K-Stand")

case object P3C4_UploadError extends PhevosException("P3C-4", "Error uploading TM tar container to KVS")
