package eu.man.phevos

package dx

package engine

package domain

package operating

package catia5

import java.io.{PrintWriter, IOException, FileWriter, FileReader}
import java.nio.file.{Path, Files}

import scala.collection.mutable.ListBuffer

import com.ibm.haploid.core.machinename
import com.ibm.haploid.core.service.{Success, Result, Failure}
import com.ibm.haploid.core.util.process.killNamedProcessWithChildren
import com.ibm.haploid.core.util.text.fromBase64String
import com.ibm.haploid.dx.engine.domain.binding.xmlElement
import com.ibm.haploid.dx.engine.domain.operating.consolecharset
import com.ibm.haploid.dx.engine.domain.operating.ExternalOperationDetail
import com.ibm.haploid.dx.engine.event.OperationCreate

import akka.event.LoggingAdapter
import dx.gep.NativeFormats
import eu.man.phevos.dx.util.interfaces.FileWithPath
import javax.xml.bind.annotation.XmlRootElement

/**
 *
 */
@XmlRootElement(name = "extractdrawing-catbatch-operation-detail")
case class ExtractLcaOperationDetail(

  private val parameters: String,

  private val rsc: Seq[String],

  val env: Map[String, String])

  extends ExternalOperationDetail("extractfromlca", "Licensing.CATSettings", parameters, rsc, Map("CNEXTOUTPUT" -> "cnextlog.txt"), "cnextlog.txt", 60 * 60 * 1000) {

  private def this() = this(null, null, null)

}

case class ParametricData(@transient inPath: Path) extends FileWithPath(inPath)

case class ExtractLcaOperator(

  operation: OperationCreate,

  basedirectory: Path,

  log: LoggingAdapter,

  timeout: Long)

  extends RunCATBatchOperator {

  type PreProcessingInput = NativeFormats

  type ProcessingInput = ParametricData

  type PostProcessingInput = ParametricData

  type PostProcessingOutput = ParametricData

  override protected[this] def processReturnCode(returncode: Int): Int = super.processReturnCode(returncode) match {
    case 0 ⇒ if (fromBase64String(getLogfile, consolecharset).contains("BATCH Run Finished, result is 0")) 0 else -1
    case c ⇒ c
  }

  protected[this] def doPreProcessing(input: PreProcessingInput): Result[ProcessingInput] = {
    operation.detail match {
      case detail: ExtractLcaOperationDetail ⇒

        val myenvname = "myEnv-" + machinename + ".txt"

        val fw = new FileWriter(workingdirectory.resolve("extractbatch.cmd").toFile)
        val pw = new PrintWriter(fw)

        val catfiles = new ListBuffer[String]
        input.nativenames.foreach((file: String) ⇒ catfiles += (file.substring(0, 23) + "." + file.substring(23)))

        try {
          pw.println("CATPDMExtractEV5Batch.exe ^")
          if (input.isAssembly) pw.println(" -prc " + input.prcname + " ^")
          catfiles.foreach(f ⇒ pw.println(" -adddoc " + f + " ^"))
          pw.println(" -completeWithImpactedBy all_level ^")
          pw.println(" -server " + server + " ^")
          pw.println(" -user " + user + " ^")
          pw.println(" -pwd " + passwd + " ^")
          pw.println(" -role \"" + role + "\" ^")
          pw.println(" -delete ^")
          pw.println(" -out " + outputdirectory)
          pw.flush
        } catch {
          case e: IOException ⇒ error("failure while writing input.cmd : " + e)
        } finally {
          pw.close
        }

        Files.createDirectories(outputdirectory)
        Success(ParametricData(outputdirectory))
    }

  }

  protected[this] def doProcessing(input: ProcessingInput) = {
    Success(input)
  }

  protected[this] def doPostProcessing(input: PostProcessingInput) = {

    killNamedProcessWithChildren("CATJPESStarter")

    if (Files.exists(workingdirectory.resolve("cnextlog.txt"))) {
      val fr = new FileReader(workingdirectory.resolve("cnextlog.txt").toString)
      val lr = new java.io.LineNumberReader(fr)
      var line: String = null
      var rc: Int = 0

      try {

        while (null != { line = lr.readLine; line }) {

          if (line.contains("application result =")) {
            debug("found : " + line)
            rc = (line.split("=")(1)).trim.toInt
            debug("set rc=" + rc)
          }
        }

      } catch {
        case e: Exception ⇒
          warning("error while parsing logfile for returncode : " + e.getMessage)
      } finally {
        fr.close
      }

      rc match {
        case 0 ⇒ Success(input)
        case 202 ⇒ Failure(new Exception("Failure while connecting to LCA."))
        case _ ⇒ Failure(new Exception("General failure while extracting files from LCA, see captured logfile."))
      }
    } else {
      warning("no file found for : " + workingdirectory.resolve("cnextlog.txt"))
      Success(input)
    }

  }

}
