package eu.man.phevos

package dx

package kvs

package services

import com.ibm.haploid.core.newLogger
import com.ibm.haploid.core.service.{ Success, Result, Service }
import com.ibm.haploid.core.util.time.now


import eu.man.phevos.dx.kvs.utils.{ StringUtils, ResponsibleUser, KStandUtils }
import eu.man.phevos.dx.kvs.KVSException
import eu.man.phevos.dx.util.interfaces.PartInfo

case class KStandDrawingDateGetInput(partinfo: PartInfo)

class KStandDrawingDateGetImpl

  extends Service[KStandDrawingDateGetInput, (Long, String)]

  with ResponsibleUser

  with KStandUtils

  with StringUtils {

  def doService(in: KStandDrawingDateGetInput): Result[(Long, String)] = {
    /**
     * technical exceptions (server not available ...) will escape and will lead to a retry of this operation
     */
    val kStand = getKStandForPartWithIndex(in.partinfo)

    try {
      def kStandNum = kStand.getString("KSTAND_KSTAND")
      newLogger(this).debug("Looking for 'KSTAND_ZEICHNUNGSDATUM' for 'KSTAND_KSTANDM' = " + kStandNum + " ...")
      try {
        Success((getTimestampFromString(kStand.getString("KSTAND_ZEICHNUNGSDATUM")), kStandNum))
      } catch {
        case e: Throwable ⇒
          Success(now, kStandNum)
      }
    } catch {
      case e: KVSException => throw e
      case e: Throwable ⇒
        Success(now, "")
    }
  }

}
