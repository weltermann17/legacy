package eu.man.phevos.dx.engine.domain.operating.kvs
import com.ibm.haploid.dx.engine.domain.binding.xmlAttribute
import eu.man.phevos.dx.kvs.services.PartNameLanguage
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
import eu.man.phevos.dx.kvs.services.GetPartNameInput
import eu.man.phevos.dx.util.interfaces.PartInfo
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.binding.xmlTransient
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "get-partname-operation-detail")
case class GetPartNameOperationDetail(

  @xmlElement(required = true) partinfo: PartInfo,

  @xmlTransient language: PartNameLanguage)

  extends OperationDetail("get-partname") {

  def this() = this(null, null)

}

class GetPartNameOperator(

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
      case GetPartNameOperationDetail(partinfo, language) ⇒
        KVSServices.GetPartName(GetPartNameInput(partinfo, language))
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput] = Success(input)
}