package eu.man.phevos.dx.util.interfaces

import java.nio.file.Path
import java.nio.file.FileSystems

class FileWithPath(@transient inPath: Path) extends Serializable {

  val strPath = inPath.toString

  @transient lazy val path: Path = FileSystems.getDefault.getPath(strPath)

}
/**
 *
 */
case class File(@transient inPath: Path) extends FileWithPath(inPath)

/**
 *
 */
case class MTBPartFile(@transient inPath: Path) extends FileWithPath(inPath) {

  lazy val metadata = MTBPartMetadata(path)

}

/**
 *
 */
abstract sealed class ReleaseLevel(val value: String)

  extends EnumClass

/**
 *
 */
object ReleaseLevel extends EnumObject[ReleaseLevel] {

  case object E extends ReleaseLevel("e")
  case object KN extends ReleaseLevel("kn")

  val values = List[ReleaseLevel](E, KN)

}

/**
 * Used in EZISMetadata
 */
object MTBIndex {

  def apply(s: String): String = {
    // Format index
    if (s.length == 3) {
      s.replace(" ", "_")
    } else {
      val t = s.trim
      if (t.length == 0)
        "___"
      else if (t.length == 1)
        "_" + t + "_"
      else if (t.length == 2)
        t + "_"
      else
        t.substring(0, 3)
    }
  }

}

/**
 * Used in EZISMetadata
 */
trait EZISMetadata {

  val partNumber: String

  protected val _index: String

  val release: ReleaseLevel

  val sheet: Int

  def index = MTBIndex(_index)

}

/**
 * Used in EZISMetadata
 */
case class EZISSheetMetadata(

  sheetPartNumber: String,

  sheetNumber: Int,

  private val _sheetIndex: String,

  sheetRelease: ReleaseLevel) extends EZISMetadata {

  val partNumber = sheetPartNumber

  val sheet = sheetNumber

  protected val _index = _sheetIndex

  val release = sheetRelease

  def sheetIndex = this.index

}

