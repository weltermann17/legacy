package eu.man

package phevos

package dx

case class Mapping(

  mtbPartNumber: String,

  mtbPartIndex: String,

  mtbDefiningIdent: String,

  mtbDefiningIdentType: String,

  vwPartNumber: String,

  vwKStand: String,

  vwChangeNumber: String,

  vwDrawingDate: String,

  vwDefiningIdent: String,

  knRelease: String,

  titleblock: String,

  mtbStandardPart: String,

  dxstatus: String,

  mtbChangeNumber: String,

  unloadFile: String)

/**
 *
 */
package object crt {

  import com.ibm.haploid.core.config._

  val mapping = Mapping(

    getString("phevos.dx.crt.mapping.mtbPartNumber"),

    getString("phevos.dx.crt.mapping.mtbPartIndex"),

    getString("phevos.dx.crt.mapping.mtbDefiningIdent"),

    getString("phevos.dx.crt.mapping.mtbDefiningIdentType"),

    getString("phevos.dx.crt.mapping.vwPartNumber"),

    getString("phevos.dx.crt.mapping.vwKStand"),

    getString("phevos.dx.crt.mapping.vwChangeNumber"),

    getString("phevos.dx.crt.mapping.vwDrawingDate"),

    getString("phevos.dx.crt.mapping.vwDefiningIdent"),

    getString("phevos.dx.crt.mapping.knRelease"),

    getString("phevos.dx.crt.mapping.titleblock"),

    getString("phevos.dx.crt.mapping.mtbDefiningIdentType"),

    getString("phevos.dx.crt.mapping.dxstatus"),

    getString("phevos.dx.crt.mapping.mtbChangeNumber"),

    "unload")

}