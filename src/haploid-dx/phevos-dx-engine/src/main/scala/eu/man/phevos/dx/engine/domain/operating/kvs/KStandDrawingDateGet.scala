package eu.man.phevos

package dx

package engine

package domain

package operating

package kvs

import java.nio.file.Path

import javax.xml.bind.annotation.XmlType

import akka.event.LoggingAdapter

import com.ibm.haploid.core.service.{ Success, Result }
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local }
import com.ibm.haploid.dx.engine.event.OperationCreate

import eu.man.phevos.dx.kvs.services.KStandDrawingDateGetInput
import eu.man.phevos.dx.kvs.KVSServices
import javax.xml.bind.annotation.XmlRootElement
import eu.man.phevos.dx.util.interfaces.PartInfo

/**
 * 
 */
@XmlRootElement(name = "kstand-drawing-date-get-operation-detail")
case class KStandDrawingDateGetOperationDetail(

  @xmlElement partinfo: PartInfo)

  extends OperationDetail("kstand-drawing-date-get") {

  def this() = this(null)

}

class KStandDrawingDateGetOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local {

  type PreProcessingInput = Unit

  type ProcessingInput = Unit

  type PostProcessingInput = (Long, String)

  type PostProcessingOutput = (Long, String)

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] = Success(input)

  protected[this] def doProcessing(input: ProcessingInput): Result[PostProcessingInput] = {
    this.operation.detail match {
      case KStandDrawingDateGetOperationDetail(partinfo) ⇒
        KVSServices.KStandDrawingDateGet(KStandDrawingDateGetInput(partinfo))
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput] = Success(input)

}
