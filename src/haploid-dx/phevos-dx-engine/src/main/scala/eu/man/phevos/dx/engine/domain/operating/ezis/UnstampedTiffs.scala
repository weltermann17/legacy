package eu.man.phevos

package dx

package engine

package domain

package operating

package ezis

import java.nio.file.{ Path, Files }
import javax.xml.bind.annotation.XmlType
import akka.event.LoggingAdapter
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.service.Failure
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local, FileHandler }
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.event.OperationCreate
import dx.ezis.EzisServices.{ MultipleTiffInputs, MultipleTiffFiles, MultipleTiffRequests, TiffInput }
import dx.ezis.EzisServices
import eu.man.phevos.dx.util.interfaces.MTBPartFile
import eu.man.phevos.dx.ezis.EzisUtil._
import eu.man.phevos.dx.ezis.PDFFoundException
import javax.xml.bind.annotation.XmlRootElement
import com.ibm.haploid.core.service.SimpleServiceException

/**
 *
 */
@XmlRootElement(name = "unstampedtiffs-operation-detail")
case class UnstampedTiffsOperationDetail(

  @xmlAttribute(required = true) description: String)

  extends OperationDetail("unstampedtiffs") {

  def this() = this(null)

}

/**
 *
 */
class UnstampedTiffsOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local

  with FileHandler {

  type PreProcessingInput = MultipleTiffRequests

  type ProcessingInput = MultipleTiffInputs

  type PostProcessingInput = MultipleTiffFiles

  type PostProcessingOutput = MultipleTiffFiles

  protected[this] def doPreProcessing(input: PreProcessingInput) = {
    input match {
      case requests: MultipleTiffRequests ⇒ {
        Success(MultipleTiffInputs(requests.tiffrequests.map(x ⇒
          TiffInput(x.partnumber, x.versionstring, x.page, workingdirectory)) toList))
      }
    }
  }

  protected[this] def doProcessing(input: ProcessingInput) = {
    EzisServices.UnstampedTiffs(input) match {
      case Success(result) ⇒
        Success(MultipleTiffFiles(result))
      case Failure(e: PDFFoundException) ⇒
        Failure(SimpleServiceException("PDF file requires foreign part header - perform manual data exchange"))
      case Failure(e) ⇒
        throw (e)
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    var files = List[MTBPartFile]()
    input.files.foreach(file ⇒ {
      moveFileToOutputDirectory(file.path) match {
        case Success(tifffile) ⇒ {
          files ++= List(MTBPartFile(tifffile))
        }
      }
    })

    Success(MultipleTiffFiles(files))
  }

}

