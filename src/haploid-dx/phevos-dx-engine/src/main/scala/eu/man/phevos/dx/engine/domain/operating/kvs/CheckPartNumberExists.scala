package eu.man.phevos.dx.engine.domain.operating.kvs
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local, FileHandler }
import akka.event.LoggingAdapter
import eu.man.phevos.dx.util.interfaces.PartInfo
import javax.xml.bind.annotation.XmlType
import com.ibm.haploid.dx.engine.event.OperationCreate
import java.nio.file.Path
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.service.Result
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.kvs.services.CheckPartNumberExistsInput
import com.ibm.haploid.core.service.Failure
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "check-part-number-exists-operation-detail")
case class CheckPartNumberExistsOperationDetail(

  @xmlElement(required = true) partinfo: PartInfo)

  extends OperationDetail("check-part-number-exists") {

  def this() = this(null)

}

/**
 *
 */
class CheckPartNumberExistsOperator(

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
      case CheckPartNumberExistsOperationDetail(partInfo) ⇒
        KVSServices.CheckPartNumberExists(CheckPartNumberExistsInput(partInfo))
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput] = Success(input)

}