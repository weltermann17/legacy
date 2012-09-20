package eu.man.phevos

package dx

package engine

package domain

package operating

package kvs

import java.nio.file.Path
import com.ibm.haploid.core.service.{ Success, Result }
import com.ibm.haploid.dx.engine.domain.binding.{ xmlAttribute, xmlElement }
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local, FileHandler }
import com.ibm.haploid.dx.engine.event.OperationCreate
import akka.event.LoggingAdapter
import javax.xml.bind.annotation.XmlType
import dx.util.interfaces.PartInfo
import dx.kvs.KVSServices
import dx.kvs.services.KVSFilenameForCPLInput
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "get-kvs-filename-for-parametric-operation-detail")
case class GetKVSFilenameForParametricOperationDetail(

  @xmlElement(required = true) partinfo: PartInfo)

  extends OperationDetail("get-kvs-filename-for-parametric") {

  def this() = this(null)

}

class GetKVSFilenameForParametricOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase {

  type PreProcessingInput = PartInfo

  type ProcessingInput = PartInfo

  type PostProcessingInput = String

  type PostProcessingOutput = String

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] = Success(input)

  protected[this] def doProcessing(input: ProcessingInput): Result[PostProcessingInput] = {

    KVSServices.GetKVSFilenameForCPL(KVSFilenameForCPLInput(input)) match {
      case Success(kvsname) => Success(kvsname)
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput): Result[PostProcessingOutput] = Success(input)

}