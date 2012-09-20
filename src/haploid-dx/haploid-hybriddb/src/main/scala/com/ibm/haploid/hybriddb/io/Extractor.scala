package com.ibm.haploid

package hybriddb

package io

import schema._

/**
 * An Extractor can extract all values from a Table into an output format O.
 */
trait Extractor[C<: Columns, O] {

  type Output = O

  def extract(table: Table[C]): Output

}

