package com.ibm.haploid

package dx

package engine

package domain

package operating

/**
 *
 */
sealed trait Locality

trait Local extends Locality

trait Remote extends Locality

