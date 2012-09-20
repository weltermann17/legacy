package com.ibm.de.ebs.plm.scala.util

import scala.math.pow
import scala.math.round

object Converters {

  import math._

  def bytesToKb(bytes: Long, precision: Int = 2) = bytesTo(bytes, 1024, precision)

  def bytesToMb(bytes: Long, precision: Int = 2) = bytesTo(bytes, 1024 * 1024, precision)

  def roundBy(d: Double, precision: Int = 2) = {
    val p = pow(10., precision)
    round(d * p) / p
  }

  private def bytesTo(bytes: Long, per: Double, precision: Int): Double = {
    val mb = bytes / per
    roundBy(bytes / per, precision)
  }
}

