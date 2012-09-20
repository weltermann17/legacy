package com.ibm.haploid

package hybriddb

package schema

package column

import collection.mutable.Builder

final class CountingBuilder 

  extends Builder[Any, Int] {

  def +=(elem: Any) = { i += 1; this }

  def result = i

  def clear = i = 0 

  private[this] var i = 0

}

