package eu.man.phevos.dx.util

package interfaces

import com.ibm.haploid.dx.engine.domain.DomainObject
import com.ibm.haploid.dx.engine.domain.binding.{ xmlAttribute, xmlTransient, xmlJavaTypeAdapter }
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import com.ibm.haploid.dx.engine.domain.binding.StringOptionAdapter
import com.ibm.haploid.dx.engine.domain.binding.LongOptionAdapter

/**
 *
 */
@XmlRootElement(name = "partinfo")
@XmlType
class PartInfo(

  @xmlAttribute val mtbPartNumber: String,

  @xmlAttribute private val _mtbPartIndex: String,

  @xmlAttribute val mtbDefiningIdent: String,

  @xmlAttribute val vwPartNumber: String,

  @xmlJavaTypeAdapter(classOf[StringOptionAdapter]) val vwKStand: Option[String],

  @xmlJavaTypeAdapter(classOf[StringOptionAdapter]) val vwChangeNumber: Option[String],

  @xmlJavaTypeAdapter(classOf[LongOptionAdapter]) val vwDrawingDate: Option[Long],

  @xmlAttribute val vwDefiningIdent: String,

  @xmlAttribute val knRelease: Boolean,

  @xmlAttribute val titleblock: Boolean,

  @xmlAttribute val mtbStandardPart: Boolean,

  @xmlAttribute val dxStatus: String,

  @xmlAttribute val mtbChangeNumber: String) extends DomainObject {

  var unloadFile: String = ""

  def this() = this(null, null, null, null, None, None, None, null, false, false, false, null, null)

  def isCOP = {
    !mtbPartNumber.startsWith("65")
  }

  def vwPartNumberShort = this.vwPartNumber.replaceAll("\\.", "")

  def mtbPartIndex: String = MTBIndex(_mtbPartIndex)

  def isEEPartnumber: Boolean = eeParts.find(mtbPartNumber.startsWith(_)).isDefined
  
  def isVariant = dxStatus.toInt == 4
  
  override def toString = List(mtbPartNumber, mtbPartIndex, mtbDefiningIdent, vwPartNumber, vwKStand, vwDefiningIdent).toString.replaceFirst("List", "PartInfo")
}

object PartInfo {

  def apply(

    mtbPartNumber: String,

    mtbPartIndex: String,

    mtbDefiningIdent: String,

    vwPartNumber: String,

    vwKStand: Option[String],

    vwChangeNumber: Option[String],

    vwDrawingDate: Option[Long],

    vwDefiningIdent: String,

    knRelease: Boolean,

    titleblock: Boolean,

    mtbStandardPart: Boolean,

    dxStatus: String,

    mtbChangeNumber: String,

    unloadFile: String): PartInfo = {

    val partinfo = new PartInfo(mtbPartNumber, mtbPartIndex, mtbDefiningIdent, vwPartNumber, vwKStand, vwChangeNumber,
      vwDrawingDate, vwDefiningIdent, knRelease, titleblock, mtbStandardPart, dxStatus, mtbChangeNumber)

    partinfo.unloadFile = unloadFile

    partinfo

  }

}