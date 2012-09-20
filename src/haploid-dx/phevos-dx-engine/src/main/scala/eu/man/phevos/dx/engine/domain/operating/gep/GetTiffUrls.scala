package eu.man.phevos

package dx

package engine

package domain

package operating

package gep

import java.nio.file.{ Path, Files }
import javax.xml.bind.annotation.XmlType
import akka.event.LoggingAdapter
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local, FileHandler }
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.event.OperationCreate
import dx.gep.{ GEPServices, LookupTiffUrlsInput, TiffUrlResult }
import eu.man.phevos.dx.util.interfaces.PartInfo
import com.ibm.haploid.core.service.Failure
import javax.xml.bind.annotation.XmlRootElement

/**
 *
 */
@XmlRootElement(name = "gettiffurls-operation-detail")
case class GetTiffUrlsOperationDetail(

  @xmlElement(required = true) partinfo: PartInfo)

  extends OperationDetail("gettiffurls") {

  def this() = this(null)

}

/**
 *
 */
class GetTiffUrlsOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase {

  type PreProcessingInput = Unit

  type ProcessingInput = Unit

  type PostProcessingInput = List[TiffUrlResult]

  type PostProcessingOutput = List[TiffUrlResult]

  protected[this] def doPreProcessing(input: PreProcessingInput) = Success(input)

  protected[this] def doProcessing(input: ProcessingInput) = {

    operation.detail match {
      case GetTiffUrlsOperationDetail(partinfo) ⇒
        GEPServices.GetList(LookupTiffUrlsInput(partinfo.mtbPartNumber, partinfo.mtbPartIndex)) match {
          case Success(tifflist) ⇒
            Success(tifflist)
        }
    }

  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    Success(input)
  }

}

