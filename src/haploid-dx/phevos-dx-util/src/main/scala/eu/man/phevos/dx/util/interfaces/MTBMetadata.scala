package eu.man.phevos.dx.util.interfaces

import java.nio.file.Path

/**
 *
 */
case class MTBPartMetadata(

  mtbPartNumber: String,

  private val _mtbPartIndex: String,

  releaseLevel: ReleaseLevel,

  page: Int,

  pda: PDA) extends EZISMetadata {

  val partNumber = mtbPartNumber

  val sheet = page

  protected val _index = _mtbPartIndex

  val release = releaseLevel

  def mtbPartIndex = this.index
}

/**
 *
 */
object MTBPartMetadata {

  def apply(s: String): MTBPartMetadata = {
    s.split("\\.")(0).length match {
      case 70 ⇒
        fromKVSFilename(s)
      case 72 ⇒
        fromKVSFilename(s, 2)
      case _ ⇒
        fromEZISFilename(s)
    }
  }

  def fromKVSFilename(s: String, shift: Int = 0): MTBPartMetadata = {
    val pda = PDA.forString(s.substring(19 + shift, 22 + shift).replace("_", "")).get

    val mtbPartNumber = s.substring(50 + shift, 63 + shift).replaceFirst("_", ".").replaceFirst("_", "-")

    val mtbSheetIndex = s.substring(64 + shift, 67 + shift)

    val releaseLevel =
      if (pda.isTiff)
        ReleaseLevel.forString(s.substring(68 + shift, 70 + shift).replace("_", "").toLowerCase).get
      else null

    val page =
      if (pda.isTiff)
        s.substring(37 + shift, 40 + shift).replace("_", "").toInt
      else
        0

    MTBPartMetadata(mtbPartNumber, mtbSheetIndex, releaseLevel, page, pda)
  }

  // TODO Mapping for PDA Type????
  def fromEZISFilename(s: String): MTBPartMetadata = {
    val nameArr = s.split("_")

    val drawingnumber = {
      def getDrawingNumber(s: String): String = {
        s.substring(0, 2) + "." + s.substring(2, 7) + "-" + s.substring(7)
      }

      getDrawingNumber(nameArr(0))
    }

    val sheet = nameArr(1).toInt

    val sheetIndex = nameArr(2).length() match {
      case 1 ⇒
        "_" + nameArr(2) + "_"
      case 2 ⇒
        nameArr(2) + "_"
      case _ ⇒
        nameArr(2)
    }

    val itr = nameArr(3)

    val status = {
      def getStatus(s: String): ReleaseLevel = {
        if (s.equals("pre")) ReleaseLevel.E
        else ReleaseLevel.KN
      }

      getStatus(nameArr(4))
    }

    MTBPartMetadata(drawingnumber, sheetIndex, status, sheet, PDA.TZ)
  }

  def apply(file: Path): MTBPartMetadata = apply(file.getFileName().toString())

}

