package com.ibm.haploid

package hybriddb

package schema

package column

/**
 * A PrimaryKey column is unique and the input sequence of values comes already sorted.
 */
trait PrimaryKey[T] extends Unique[T]
