package eu.man.phevos.dx.engine.domain.operating.kvs
import eu.man.phevos.dx.util.interfaces.PartInfo
import javax.xml.bind.annotation.XmlElement
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
import eu.man.phevos.dx.kvs.services.AttachDVOfHighestIndexOfSheetToKStandInput
import eu.man.phevos.dx.util.interfaces.MTBPartMetadata
import eu.man.phevos.dx.util.interfaces.ReleaseLevel
import eu.man.phevos.dx.util.interfaces.PDA
import eu.man.phevos.dx.util.interfaces.EZISSheetMetadata
import eu.man.phevos.dx.util.interfaces.EZISMetadata
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "attach-dv-of-highest-index-of-sheet-to-kstand-operation-detail")
case class AttachDVOfHighestIndexOfSheetToKStandOperationDetail(

  @xmlElement(required = true) partinfo: PartInfo)

  extends OperationDetail("attach-dv-of-highest-index-of-sheet-to-kstand") {

  def this() = this(null)

}

class AttachDVOfHighestIndexOfSheetToKStandOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local {

  type PreProcessingInput = EZISMetadata

  type ProcessingInput = EZISMetadata

  type PostProcessingInput = Boolean

  type PostProcessingOutput = Boolean

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] = Success(input)

  protected[this] def doProcessing(input: ProcessingInput): Result[PostProcessingInput] = {

    operation.detail match {

      case AttachDVOfHighestIndexOfSheetToKStandOperationDetail(partinfo) ⇒
        KVSServices.AttachDVOfHighestIndexOfSheetToKStand(AttachDVOfHighestIndexOfSheetToKStandInput(partinfo, input))

    }

  }

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput] = Success(input)

}
