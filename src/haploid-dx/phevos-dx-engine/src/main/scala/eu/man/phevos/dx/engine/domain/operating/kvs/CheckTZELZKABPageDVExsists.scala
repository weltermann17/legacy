package eu.man.phevos.dx.engine.domain.operating.kvs
import com.ibm.haploid.dx.engine.domain.operating.OperationDetail
import javax.xml.bind.annotation.XmlElement
import eu.man.phevos.dx.util.interfaces.PartInfo
import javax.xml.bind.annotation.XmlType
import akka.event.LoggingAdapter
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.kvs.services.CheckTZELZKABPageExistsInput
import com.ibm.haploid.dx.engine.domain.operating.OperatorBase
import com.ibm.haploid.dx.engine.event.OperationCreate
import java.nio.file.Path
import com.ibm.haploid.dx.engine.domain.operating.FileHandler
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.service.Result
import com.ibm.haploid.dx.engine.domain.operating.Local
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "check-tz-elz-kab-page-dv-exists-operation-detail")
case class CheckTZELZKABPageDVExsistsOperationDetail(

  @XmlElement partinfo: PartInfo)

  extends OperationDetail("check-tz-elz-kab-page-dv-exists") {

  def this() = this(null)

}

class CheckTZELZKABPageDVExsistsOperator(

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
      case CheckTZELZKABPageDVExsistsOperationDetail(partinfo) ⇒
        KVSServices.CheckTZELZKABPageDVExist(CheckTZELZKABPageExistsInput(partinfo))
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput] = Success(input)
}