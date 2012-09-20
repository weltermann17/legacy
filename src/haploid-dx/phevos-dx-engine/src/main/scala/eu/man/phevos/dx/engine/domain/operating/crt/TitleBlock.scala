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
import com.ibm.haploid.dx.engine.event.OperationCreate
import com.ibm.haploid.dx.engine.domain.binding._
import dx.crt.CrtServices.{ TitleBlockFlag, TitleBlock, Identifier }
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "titleblock-operation-detail")
case class TitleblockOperationDetail(

  @xmlAttribute(required = true) dummy: String)

  extends OperationDetail("titleblock") {

  def this() = this(null)
}

class TitleblockOperator(
  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase 
  
  with Local {

  type PreProcessingInput = Identifier

  type ProcessingInput = Identifier

  type PostProcessingInput = TitleBlockFlag

  type PostProcessingOutput = TitleBlockFlag

  protected[this] def doPreProcessing(input: PreProcessingInput) = {
    Success(input)
  }

  protected[this] def doProcessing(input: ProcessingInput) = {

    operation.detail match {

      case detail @ TitleblockOperationDetail(dummy) ⇒ TitleBlock(input)

    }

  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    Success(input)
  }

}