package com.ibm.haploid

package hybriddb

package io

import schema._

/**
 * A Filler can fill a Table with values.
 */
trait Filler {

  def fill: Table[_ <: Columns]

}

