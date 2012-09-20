package eu.man.phevos

package dx

package engine

package domain

package operating

package catia5

import java.nio.file.{ Path }
import javax.xml.bind.annotation.XmlType
import akka.event.LoggingAdapter
import com.ibm.haploid.core.service.{ Success, Failure }
import com.ibm.haploid.dx.engine.domain.operating.{ OperatorBase, OperationDetail, Local, FileHandler }
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.event.OperationCreate
import dx.gep.{ GEPServices }
import dx.gep._
import eu.man.phevos.dx.gep.PartMetadataInput
import java.nio.file.Paths
import eu.man.phevos.dx.util.interfaces.File
import java.nio.file.Files
import collection.JavaConversions._
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "dummy-get-parametric-from-drawing-operation-detail")
case class DummyGetParametricFromDrawingOperationDetail(

  @xmlElement(required = true) dummy: String)

  extends OperationDetail("dummy-get-parametric-from-drawing") {

  def this() = this(null)

}

//case class ParametricData(@transient path: Path)

class DummyGetParametricFromDrawingOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends OperatorBase

  with Local

  with FileHandler {

  type PreProcessingInput = String

  type ProcessingInput = Unit

  type PostProcessingInput = ParametricData

  type PostProcessingOutput = ParametricData

  protected[this] def doPreProcessing(input: PreProcessingInput) = {
    val files = Files.newDirectoryStream(Paths.get("C:\\Entwicklung\\PHEVOS DX\\Parametric_example\\new"), "*.{CATPart,CATProduct,CATDrawing}")
    files.iterator.foreach(path ⇒ {
      copyFileToWorkingDirectory(path)
    })
    files.close

    Success(())
  }

  protected[this] def doProcessing(input: ProcessingInput) = {
    operation.detail match {
      case DummyGetParametricFromDrawingOperationDetail(dummy) ⇒
        Success(ParametricData(outputdirectory))
    }

  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {
    val files = Files.newDirectoryStream(workingdirectory, "*.{CATPart,CATProduct,CATDrawing}")
    files.iterator.foreach(path ⇒ {
      moveFileToOutputDirectory(Paths.get(workingdirectory.toAbsolutePath.toString + "\\" + path.getFileName.toString))
    })
    files.close
    Success(input)
  }

}