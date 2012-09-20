package eu.man.phevos

package dx

package engine

package domain

package operating

package catia5

import java.nio.file.{ Paths, Path }
import javax.xml.bind.annotation.XmlType
import akka.event.LoggingAdapter
import com.ibm.haploid.core.machinename
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.dx.engine.domain.operating.{ ExternalOperator, ExternalOperationDetail }
import com.ibm.haploid.dx.engine.domain.binding._
import com.ibm.haploid.dx.engine.event.OperationCreate
import com.ibm.haploid.dx.engine.domain.operating._
import com.ibm.haploid.core.service.Failure
import com.ibm.haploid.core.util.nextAffinity

/**
 *
 */
@XmlType(name = "run-catbatch-operation-detail")
case class RunCATBatchOperationDetail(

  private val operatorname: String,

  private val batchname: String,

  private val parameter: String,

  private val rsc: Seq[String],

  private val env: Map[String, String],

  private val lfile: String,

  private val tmout: Long)

  extends ExternalOperationDetail(operatorname, batchname, parameter, rsc, env, lfile, tmout) {

  private def this() = this(null, null, null, null, null, null, -1L)

}

/**
 *
 */
trait RunCATBatchOperator

  extends ExternalOperator {

  override protected[this] def processReturnCode(returncode: Int): Int = returncode match {
    case 0 ⇒ 0
    case e ⇒
      log.info("Found an unknown or invalid returcode, will ignore it by setting it to 0 : " + e)
      0
  }

  override protected[this] def internalPreProcessing() = {
    super.internalPreProcessing match {
      case Success(_) ⇒
        val details = operation.detail.asInstanceOf[ExternalOperationDetail]
        val myenvname = "myEnv-" + machinename + ".txt"
        val catenv = Paths.get(myenvname)
        
        copyTextResourceToWorkingDirectory(catenv) match {
          case succ @ Success(fullpath) ⇒
            fullpath.toFile.setExecutable(true)

            commandline = "C:\\Windows\\system32\\cmd.exe /c start \"" +
              workingdirectory.hashCode +
              "\" /affinity 0x" +
              nextAffinity + " /B /wait " + pathtocatstart + " -s -env " + myenvname + " -direnv " + workingdirectory +
              " -run \"C:\\Windows\\system32\\cmd.exe /c " + workingdirectory.resolve("extractbatch.cmd") + "\""

            succ
        }

    }
  }
}

/**
 * for testing
 */
class SimpleRunCATBatchOperator(

  val operation: OperationCreate,

  val basedirectory: Path,

  val log: LoggingAdapter,

  val timeout: Long)

  extends RunCATScriptOperator {

  type PreProcessingInput = Unit

  type ProcessingInput = Unit

  type PostProcessingInput = Unit

  type PostProcessingOutput = Unit

  protected[this] def doPreProcessing(input: Unit) = Success(input)

  protected[this] def doProcessing(input: Unit) = Success(input)

  protected[this] def doPostProcessing(input: Unit) = Success(input)

}

