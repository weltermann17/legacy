package eu.man.phevos

package dx

package kvs

import com.ibm.haploid.core.service.ServiceException

import services.{ ValidateKStandImpl, UploadFileImpl, UpdateKStandImpl, ResponsibleUserImpl, KStandDrawingDateGetImpl, GetPartNameImpl, GetKVSFilenameForTIFFImpl, GetKVSFilenameForDMUImpl, GetKVSFilenameForCPLImpl, GetHighestKStandImpl, CheckTZELZKABPageDVExistImpl, CheckPartNumberExistsImpl, CheckParametricDocumentVersionAlreadyExistsImpl, CheckMTBIndexIsSupercededImpl, CheckDVWithSameOrHigherIndexExistsImpl, CheckDVForEZISFileExistsImpl, CheckDMUDVExistsImpl, AttachPageDVToKStandImpl, AttachDVOfHighestIndexOfSheetToKStandImpl }

/**
 * A "business-logic" exception not a technical one.
 */
class KVSException(message: String)

  extends Exception(message)

  with ServiceException
  
object KVSException { def apply(message: String): KVSException = new KVSException(message) }

class KStandNotFoundException(message: String) extends KVSException("KStand not found (" + message + ")")

/**
 *
 */
object KVSServices {

  object AttachDVOfHighestIndexOfSheetToKStand extends AttachDVOfHighestIndexOfSheetToKStandImpl

  object AttachPageDVToKStand extends AttachPageDVToKStandImpl

  object CheckDMUDVExists extends CheckDMUDVExistsImpl

  object CheckDVForEZISFileExists extends CheckDVForEZISFileExistsImpl

  object CheckDVWithSameOrHigherIndexExists extends CheckDVWithSameOrHigherIndexExistsImpl

  object CheckMTBIndexIsSuperceded extends CheckMTBIndexIsSupercededImpl

  object CheckParametricDocumentVersionAlreadyExist extends CheckParametricDocumentVersionAlreadyExistsImpl

  object CheckPartNumberExists extends CheckPartNumberExistsImpl

  object CheckTZELZKABPageDVExist extends CheckTZELZKABPageDVExistImpl

  object GetHighestKStand extends GetHighestKStandImpl

  object GetKVSFilenameForCPL extends GetKVSFilenameForCPLImpl

  object GetKVSFilenameForDMU extends GetKVSFilenameForDMUImpl

  object GetKVSFilenameForTIFF extends GetKVSFilenameForTIFFImpl

  object GetPartName extends GetPartNameImpl

  object KStandDrawingDateGet extends KStandDrawingDateGetImpl

  object ResponsibleUser extends ResponsibleUserImpl

  object UpdateKStand extends UpdateKStandImpl

  object UploadFile extends UploadFileImpl

  object ValidateKStand extends ValidateKStandImpl

}