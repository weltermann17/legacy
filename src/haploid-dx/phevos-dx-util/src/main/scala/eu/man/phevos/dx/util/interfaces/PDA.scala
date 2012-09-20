package eu.man.phevos.dx.util.interfaces

/**
 *
 */
trait EnumClass extends Serializable {

  val value: String

}

/**
 *
 */
trait EnumObject[T <: EnumClass] extends Serializable {

  val values: List[T]

  def forString(s: String): Option[T] = values.find(e ⇒ s.equals(e.value))

}

/**
 *
 */
abstract sealed class PDA(val value: String, val isTiff: Boolean)

  extends EnumClass

/**
 *
 */
object PDA extends EnumObject[PDA] {

  case object CPL extends PDA("CPL", false)
  case object ELZ extends PDA("ELZ", true)
  case object KAB extends PDA("KAB", true)
  case object TM extends PDA("TM", false)
  case object TZ extends PDA("TZ", true)

  val values = List[PDA](CPL, ELZ, KAB, TM, TZ)

}

