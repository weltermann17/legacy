package eu.man.phevos.dx.util.interfaces

case class MTBPartIndexOrdering(me: String)

  extends Ordered[String] {

  def compare(that: String): Int = {

    if (!checkMTBPartIndex(that) || !checkMTBPartIndex(me))
      throw new IllegalArgumentException

    def isNumeric(input: String): Boolean = input.forall(_.isDigit)

    def getAdjustedIndexforCompare(inIndex: String): String = {

      val trimIndex = inIndex.replace("_", "").trim

      trimIndex.length match {
        case 0 ⇒
          trimIndex
        case 1 ⇒
          trimIndex + "0"

        case 2 ⇒
          val fc = trimIndex.substring(0, 1)
          val sc = trimIndex.substring(1, 2)

          if (!isNumeric(sc) && !isNumeric(fc)) {
            trimIndex + "0"
          } else if (!isNumeric(fc) && isNumeric(sc)) {
            trimIndex
          } else {
            trimIndex + "00"
          }

        case 3 ⇒
          val fc = trimIndex.substring(0, 1)
          val sc = trimIndex.substring(1, 2)
          val tc = trimIndex.substring(2, 3)

          if (!isNumeric(fc) && !isNumeric(sc) && isNumeric(tc)) {
            trimIndex
          } else if (isNumeric(fc) && isNumeric(sc) && isNumeric(tc)) {
            trimIndex + "0"
          } else {
            throw new IllegalArgumentException("PartIndex " + inIndex + " is not allowed.")
          }
        case _ ⇒
          throw new IllegalArgumentException("Index is not allowed to be longer than 3 chars.")
      }

    }

    val adjustedMe = getAdjustedIndexforCompare(me)
    val adjustedThat = getAdjustedIndexforCompare(that)

    if (adjustedMe.length > adjustedThat.length) 1
    else if (adjustedMe.length < adjustedThat.length) -1
    else {

      if (adjustedMe.equals(adjustedThat)) 0
      else if (adjustedMe < adjustedThat) -1
      else 1

    }
  }

  def ==(that: String) = compare(that) == 0

  // TODO check if syntax is correct
  def checkMTBPartIndex(s: String): Boolean = {
    s.length() == 3
  }

}

