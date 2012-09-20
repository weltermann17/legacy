package eu.man.phevos

package dx

package engine

package domain

package operating

package kvs

import java.nio.file.Path
import com.ibm.haploid.core.service.Result
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.operating.FileHandler
import com.ibm.haploid.dx.engine.domain.operating.Local
import com.ibm.haploid.dx.engine.domain.operating.OperationDetail
import com.ibm.haploid.dx.engine.domain.operating.OperatorBase
import com.ibm.haploid.dx.engine.event.OperationCreate
import akka.event.LoggingAdapter
import eu.man.phevos.dx.kvs.services.UpdateKStandInput
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.util.interfaces.PartInfo
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlElement

@XmlRootElement(name = "update-kstand-operation-detail")
case class UpdateKStandOperationDetail(

  @XmlElement(required = true) partinfo: PartInfo)

  extends OperationDetail("update-kstand") {

  def this() = this(null)

}

class UpdateKStandOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local {

  type PreProcessingInput = Unit

  type ProcessingInput = Unit

  type PostProcessingInput = Boolean

  type PostProcessingOutput = Boolean

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] = Success(input)

  protected[this] def doProcessing(input: ProcessingInput): Result[PostProcessingInput] = {
    operation.detail match {
      case UpdateKStandOperationDetail(partinfo) ⇒
        KVSServices.UpdateKStand(UpdateKStandInput(partinfo))
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput] = Success(input)

}