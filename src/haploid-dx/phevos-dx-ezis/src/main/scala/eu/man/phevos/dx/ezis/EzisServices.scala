package eu.man.phevos

package dx

package ezis

import java.nio.file.Path
import com.ibm.haploid.core.service._
import util.interfaces.MTBPartMetadata
import eu.man.phevos.dx.util.interfaces.MTBPartFile

object EzisServices {

  case class TiffInput(partnumber: String, versionstring: String, page: Int, @transient workingdir: Path)
  case class MultipleTiffInputs(@transient tiffinputs: List[TiffInput])
  case class TiffRequest(partnumber: String, versionstring: String, page: Int)
  case class MultipleTiffRequests(tiffrequests: List[TiffRequest])

  case class MultipleTiffFiles(@transient files: List[MTBPartFile])

  object UnstampedTiff extends Service[TiffInput, MTBPartFile] {

    def doService(input: TiffInput): Result[MTBPartFile] = {
      Success(EzisUtil.getUnstampedTiff(input.workingdir, input.partnumber, input.page, input.versionstring))
    }

  }

  object UnstampedTiffs extends Service[MultipleTiffInputs, List[MTBPartFile]] {

    def doService(input: MultipleTiffInputs): Result[List[MTBPartFile]] = {
      val result = EzisUtil.getUnstampedTiffs(input.tiffinputs(0).workingdir, input.tiffinputs)
      Success(result)
    }

  }

}