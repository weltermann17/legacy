package eu.man.phevos

package dx

package engine

package domain

package operating

package crt

import java.nio.file.Path
import javax.xml.bind.annotation.XmlType
import akka.event.LoggingAdapter
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local }
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.event.OperationCreate
import dx.crt.CrtServices.{ Sheets, Identifier }
import dx.crt.CrtServices
import eu.man.phevos.dx.util.interfaces.PartInfo
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "validsheets-operation-detail")
case class ValidSheetsOperationDetail(

  @xmlElement(required = true) partinfo: PartInfo)

  extends OperationDetail("validsheets") {

  def this() = this(null)
}

class ValidSheetsOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local {

  type PreProcessingInput = Unit

  type ProcessingInput = Unit

  type PostProcessingInput = Sheets

  type PostProcessingOutput = Sheets

  protected[this] def doPreProcessing(input: PreProcessingInput) = {
    Success(input)
  }

  protected[this] def doProcessing(input: ProcessingInput) = {
    operation.detail match {
      case ValidSheetsOperationDetail(partinfo) ⇒
        CrtServices.ValidSheets(Identifier(partinfo.mtbPartNumber, partinfo.mtbPartIndex, partinfo.vwPartNumber, partinfo.vwKStand))
    }
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    Success(input)
  }

}