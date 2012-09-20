package eu.man.phevos

package dx

package engine

package domain

package operating

package gep

import java.nio.file.Path

import com.ibm.haploid.core.service.{Success, Failure}
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.operating.{OperatorBase, OperationDetail}
import com.ibm.haploid.dx.engine.event.OperationCreate

import akka.event.LoggingAdapter
import dx.gep.{NativeFormats, GEPServices}
import eu.man.phevos.dx.util.interfaces.PartInfo
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "getnativeformats-operation-detail")
case class GetNativeFormatsDetail(

  @xmlElement(required = true) partinput: PartInfo)

  extends OperationDetail("get-native-formats") {

  def this() = this(null)

}

class GetNativeFormatsOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase {

  type PreProcessingInput = Unit

  type ProcessingInput = Unit

  type PostProcessingInput = NativeFormats

  type PostProcessingOutput = NativeFormats

  protected[this] def doPreProcessing(input: PreProcessingInput) = {
    Success(())
  }

  protected[this] def doProcessing(input: ProcessingInput) = {
    operation.detail match {
      case GetNativeFormatsDetail(partinput) ⇒
        GEPServices.GetNativeCatFormats(partinput) match {
          case Success(natives) ⇒
            Success(natives)
          case Failure(reason) ⇒
            Failure(reason)
        }
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = Success(input)

}