package eu.man.phevos

package dx

package engine

package domain

package operating

package catia5

import java.nio.file.Path
import java.nio.file.Paths

import com.ibm.haploid.core.machinename
import com.ibm.haploid.core.service.Success
import com.ibm.haploid.core.util.nextAffinity
import com.ibm.haploid.dx.engine.domain.operating.ExternalOperationDetail
import com.ibm.haploid.dx.engine.domain.operating.ExternalOperator
import com.ibm.haploid.dx.engine.event.OperationCreate

import akka.event.LoggingAdapter
import javax.xml.bind.annotation.XmlRootElement

/**
 *
 */
@XmlRootElement(name = "run-catscript-operation-detail")
case class RunCATScriptOperationDetail(

  private val operatorname: String,

  private val scr: String,

  private val modname: String,

  private val rsc: Seq[String],

  private val env: Map[String, String],

  private val lfile: String,

  private val tmout: Long)

  extends ExternalOperationDetail(operatorname, scr, modname, rsc, env, lfile, tmout) {

  private def this() = this(null, null, null, null, null, null, -1L)

}

/**
 *
 */
trait RunCATScriptOperator

  extends ExternalOperator {

  override protected[this] def processReturnCode(returncode: Int): Int = returncode match {
    case 0 | 19 ⇒ 0
    case c ⇒
      log.info("Found an unknown or invalid returcode, will ignore it by setting it to 0 : " + c)
      0
  }

  override protected[this] def internalPreProcessing = {
    super.internalPreProcessing match {
      case Success(_) ⇒
        val details = operation.detail.asInstanceOf[ExternalOperationDetail]
        val myenvname = "myEnv-" + machinename + ".txt"
        val catenv = Paths.get(myenvname)

        copyTextResourceToWorkingDirectory(catenv) match {
          case succ @ Success(fullpath) ⇒
            fullpath.toFile.setExecutable(true)
            debug("pathtocatstart : " + pathtocatstart)
            commandline = "C:\\Windows\\system32\\cmd.exe /c start \"" + workingdirectory.hashCode + "\" /affinity 0x" + nextAffinity + " /B /wait " + pathtocatstart +
              " -s -env " + myenvname + " -direnv " +
              workingdirectory + (if (details.script.endsWith("CATScript"))
                " -object \"-nowindow -batch -macro " + workingdirectory.resolve(details.script) + "\""
              else
                " -run \"CNEXT.exe -nowindow -batch -macro " + workingdirectory.resolve(details.script) + " " + details.modulname + "\"")
            debug("commandline : " + commandline)
            succ
        }
    }
  }

}

/**
 * for testing
 */
class SimpleRunCATScriptOperator(

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

