package eu.man.phevos.dx.engine.domain.operating

import java.nio.file.{ Path, Files }

import javax.xml.bind.annotation.XmlType

import akka.event.LoggingAdapter

import com.ibm.haploid.core.service.{ Success, Failure }
import com.ibm.haploid.core.util.time.timeMillis
import com.ibm.haploid.core.util.text.stackTraceToString
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationResultDetail, OperationFailed, OperationDetail, Local, FileHandler }
import com.ibm.haploid.dx.engine.event.OperationCreate

import eu.man.phevos.dx.ezis.EzisServices
import eu.man.phevos.dx.ezis.EzisServices.{ TiffInput, TiffFile }

/**
 *
 */
@SerialVersionUID(1234L)
@XmlType(name = "unstampedtiff-operation-detail")
case class UnstampedTiffOperationDetail(

  @xmlAttribute(required = true) partnumber: String,

  @xmlAttribute(required = true) page: String,

  @xmlAttribute(required = true) versionstring: String)

  extends OperationDetail("unstampedtiff") {

  def this() = this(null, null, null)

}

/**
 * Output
 */
@XmlType(name = "unstampedtiff-operation-result-detail")
case class UnstampedTiffOperationResultDetail(

  success: Boolean,

  @xmlElement tifffile: String,

  @xmlJavaTypeAdapter(classOf[CDataAdapter]) reason: String,

  @xmlJavaTypeAdapter(classOf[CDataAdapter]) logging: String,

  @xmlAttribute(required = true) durationinmilliseconds: Long)

  extends OperationResultDetail(success) {

  def this() = this(false, null, null, null, -1L)

}

/**
 *
 */
class UnstampedTiffOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase with Local with FileHandler {

  protected[this] def doPreProcessing(input: ProcessResult): ProcessResult = {

    debug("UnstampedTiffOperator.doPreProcessing")
    operation.detail match {
      case detail @ UnstampedTiffOperationDetail(pnum, bl, ind) ⇒ {
        try {
          Files.createDirectories(workingdirectory)
          Files.createDirectories(outputdirectory)
          Success(detail)
        } catch {
          case e: Exception ⇒ Failure(e)
        }
      }
    }
  }

  protected[this] def doProcessing(input: ProcessResult): ProcessResult = {

    debug("UnstampedTiffOperator.doProcessing")
    operation.detail match {
      case detail @ UnstampedTiffOperationDetail(partnumber, page, versionstring) ⇒ {
        val (result, ms) = timeMillis(EzisServices.UnstampedTiff(
          TiffInput(
            detail.partnumber,
            detail.versionstring,
            detail.page.toInt,
            workingdirectory)))
        duration = ms
        result match {
          case succ @ Success(fullpath) ⇒ {
            debug("doProcessing - succ = " + fullpath)
            succ
          }
          case fail @ Failure(e) ⇒ {
            debug("doProcessing - fail = " + fail)
            Failure(OperationFailed(UnstampedTiffOperationResultDetail(false, null, e.toString, currentLog, duration)))
          }
        }
      }
    }
  }

  protected[this] def doPostProcessing(input: ProcessResult): ProcessResult = {
    debug("UnstampedTiffOperator.doPostProcessing")
    try {
      input match {
        case Success(success) ⇒ {
          val tiff = success.asInstanceOf[TiffFile].path
          moveFileToOutputDirectory(tiff) match {
            case Success(tifffile) ⇒ Success(UnstampedTiffOperationResultDetail(true, tifffile.toString, null, currentLog, duration))
            case Failure(e) ⇒ Failure(OperationFailed(UnstampedTiffOperationResultDetail(false, null, e.toString, currentLog, duration)))
          }
        }
        case fail @ Failure(_) ⇒ fail
      }
    } catch {
      case e: Exception ⇒ Failure(e)
    }
  }

  private[this] var duration = -1L

}
