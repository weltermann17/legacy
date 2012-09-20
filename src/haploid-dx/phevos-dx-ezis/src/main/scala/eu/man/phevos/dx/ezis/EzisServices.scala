package eu.man.phevos

package dx

package ezis

import java.nio.file.Path
import com.ibm.haploid.core.service._

object EzisServices {

  case class TiffInput(partnumber: String, versionstring: String, page: Int, workingdir: Path)
  case class TiffFile(path: Path)

  object UnstampedTiff extends Service[TiffInput, TiffFile] {

    def doService(input: TiffInput): Result[TiffFile] = {
      Success(EzisUtil.getUnstampedTiff(input.workingdir, input.partnumber, input.page, input.versionstring))
    }

  }

  object StampedTiff extends Service[TiffInput, TiffFile] {

    def doService(input: TiffInput): Result[TiffFile] = {
      Success(EzisUtil.getStampedTiff(input.workingdir, input.partnumber, input.page, input.versionstring))
    }

  }
}