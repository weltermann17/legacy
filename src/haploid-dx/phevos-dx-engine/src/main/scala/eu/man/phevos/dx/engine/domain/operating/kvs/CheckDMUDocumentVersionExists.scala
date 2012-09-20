package eu.man.phevos

package dx

package engine

package domain

package operating

package kvs

import eu.man.phevos.dx.util.interfaces.PartInfo
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.operating.OperationDetail
import javax.xml.bind.annotation.XmlType
import akka.event.LoggingAdapter
import com.ibm.haploid.dx.engine.domain.operating.OperatorBase
import com.ibm.haploid.dx.engine.event.OperationCreate
import java.nio.file.Path
import com.ibm.haploid.dx.engine.domain.operating.FileHandler
import com.ibm.haploid.dx.engine.domain.operating.Local
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.service.Result
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.kvs.services.ValidateKStandInput
import com.ibm.haploid.core.service.Failure
import eu.man.phevos.dx.kvs.services.CheckDMUDVExistsInput
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "check-dmu-document-exists-operation-detail")
case class CheckDMUDocumentVersionExistsOperationDetail(

  @xmlElement(required = true) partInfo: PartInfo)

  extends OperationDetail("check-dmu-document-exists") {

  def this() = this(null)

}

class CheckDMUDocumentVersionExistsOperator(

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
      case CheckDMUDocumentVersionExistsOperationDetail(partInfo) ⇒
        KVSServices.CheckDMUDVExists(CheckDMUDVExistsInput(partInfo))
    }

  }

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput] = Success(input)

}