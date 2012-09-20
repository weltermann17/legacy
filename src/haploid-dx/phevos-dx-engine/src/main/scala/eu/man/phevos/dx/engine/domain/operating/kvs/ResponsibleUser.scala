package eu.man.phevos.dx.engine.domain.operating.kvs
import eu.man.phevos.dx.util.interfaces.PartInfo
import com.ibm.haploid.core.service.{ Success, Result }
import com.ibm.haploid.dx.engine.domain.operating.OperationDetail
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import javax.xml.bind.annotation.XmlType
import akka.event.LoggingAdapter
import com.ibm.haploid.dx.engine.domain.operating.OperatorBase
import com.ibm.haploid.dx.engine.event.OperationCreate
import java.nio.file.Path
import com.ibm.haploid.dx.engine.domain.operating.FileHandler
import com.ibm.haploid.dx.engine.domain.operating.Local
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.kvs.services.ResponsibleUserInput
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "responsible-user-operation-detail")
case class ResponsibleUserOperationDetail(

  @xmlElement partinfo: PartInfo)

  extends OperationDetail("get-responsible-user") {

  def this() = this(null)

}

class ResponsibleUserOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local {

  type PreProcessingInput = Unit

  type ProcessingInput = Unit

  type PostProcessingInput = String

  type PostProcessingOutput = String

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] = Success(input)

  protected[this] def doProcessing(input: ProcessingInput): Result[PostProcessingInput] = {
    operation.detail match {
      case ResponsibleUserOperationDetail(partinfo) ⇒
        KVSServices.ResponsibleUser(ResponsibleUserInput(partinfo))
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput] = Success(input)
}