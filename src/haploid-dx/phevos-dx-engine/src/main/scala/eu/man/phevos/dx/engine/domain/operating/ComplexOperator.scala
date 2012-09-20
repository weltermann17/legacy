package eu.man.phevos

package dx

package engine

package domain

package operating

import com.ibm.haploid.dx.engine.domain.binding.xmlAttribute
import com.ibm.haploid.dx.engine.domain.operating.OperationDetail
import javax.xml.bind.annotation.XmlType
import akka.event.LoggingAdapter
import com.ibm.haploid.dx.engine.domain.operating.OperatorBase
import com.ibm.haploid.dx.engine.event.OperationCreate
import java.nio.file.Path
import com.ibm.haploid.dx.engine.domain.operating.Local
import eu.man.phevos.dx.engine.domain.operating.kvs.UploadKVSOperationDetail
import eu.man.phevos.dx.kvs.KVSServices
import eu.man.phevos.dx.kvs.services.GetKVSFilenameForTIFFInput
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.service.Result
import javax.xml.bind.annotation.XmlRootElement

/**
 * Input data
 */
@XmlRootElement(name = "complex-operation-detail")
case class ComplexOperationDetail(

  number: Int)

  extends OperationDetail("complex") {

  def this() = this(0)

}

class ComplexOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local {

  type PreProcessingInput = Int

  type ProcessingInput = Int

  type PostProcessingInput = Int

  type PostProcessingOutput = Int

  protected[this] def doPreProcessing(input: PreProcessingInput) = Success(input)

  protected[this] def doProcessing(input: ProcessingInput): Result[PostProcessingInput] = {
    log.info("EXECUTE COMPLEX OPERATOR " + System.currentTimeMillis())

    operation.detail match {
      case ComplexOperationDetail(number) ⇒
        Success(number + input)
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = Success(input)

}