package eu.man.phevos.dx.engine.domain.operating.kvs
import javax.xml.bind.annotation.XmlType
import eu.man.phevos.dx.util.interfaces.PartInfo
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.operating.OperationDetail
import akka.event.LoggingAdapter
import com.ibm.haploid.dx.engine.domain.operating.OperatorBase
import com.ibm.haploid.dx.engine.event.OperationCreate
import java.nio.file.Path
import com.ibm.haploid.dx.engine.domain.operating.FileHandler
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.service.Result
import com.ibm.haploid.dx.engine.domain.operating.Local
import eu.man.phevos.dx.util.interfaces.MTBPartMetadata
import eu.man.phevos.dx.util.interfaces.ReleaseLevel
import eu.man.phevos.dx.util.interfaces.PDA
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.kvs.services.CheckDVWithSameOrHigherIndexExistsInput
import eu.man.phevos.dx.util.interfaces.EZISSheetMetadata
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "check-dv-with-same-or-higher-index-exists-operation-detail")
case class CheckDVWithSameOrHigherIndexExistsOperationDetail(

  @xmlElement(required = true) partinfo: PartInfo)

  extends OperationDetail("check-dv-with-same-or-higher-index-exists") {

  def this() = this(null)

}

class CheckDVWithSameOrHigherIndexExistsOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local {

  type PreProcessingInput = EZISSheetMetadata

  type ProcessingInput = EZISSheetMetadata

  type PostProcessingInput = (Boolean, String)

  type PostProcessingOutput = (Boolean, String)

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] = Success(input)

  protected[this] def doProcessing(input: ProcessingInput): Result[PostProcessingInput] = {

    operation.detail match {
      case CheckDVWithSameOrHigherIndexExistsOperationDetail(partinfo) ⇒
        KVSServices.CheckDVWithSameOrHigherIndexExists(CheckDVWithSameOrHigherIndexExistsInput(partinfo, input))
    }

  }

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput] = Success(input)
}