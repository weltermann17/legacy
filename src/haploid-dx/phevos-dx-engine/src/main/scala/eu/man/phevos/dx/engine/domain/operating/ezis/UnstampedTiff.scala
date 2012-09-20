package eu.man.phevos

package dx

package engine

package domain

package operating

package ezis

import java.nio.file.Path

import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.operating.{OperatorBase, OperationDetail, Local, FileHandler}
import com.ibm.haploid.dx.engine.event.OperationCreate

import akka.event.LoggingAdapter
import dx.ezis.EzisServices.{TiffRequest, TiffInput}
import dx.ezis.EzisServices
import eu.man.phevos.dx.util.interfaces.MTBPartFile
import javax.xml.bind.annotation.XmlRootElement

/**
 *
 */
@XmlRootElement(name = "unstampedtiff-operation-detail")
case class UnstampedTiffOperationDetail(

  @xmlElement(required = true) description: String)

  extends OperationDetail("unstampedtiff") {

  def this() = this(null)

}

/**
 *
 */
class UnstampedTiffOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local

  with FileHandler {

  type PreProcessingInput = TiffRequest

  type ProcessingInput = TiffInput

  type PostProcessingInput = MTBPartFile

  type PostProcessingOutput = MTBPartFile

  protected[this] def doPreProcessing(input: PreProcessingInput) = {
    Success(TiffInput(input.partnumber, input.versionstring, input.page, workingdirectory))
  }

  protected[this] def doProcessing(input: ProcessingInput) = {
    EzisServices.UnstampedTiff(input)
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    moveFileToOutputDirectory(input.path) match {
      case Success(tifffile) ⇒ Success(MTBPartFile(tifffile))
    }
  }

}

