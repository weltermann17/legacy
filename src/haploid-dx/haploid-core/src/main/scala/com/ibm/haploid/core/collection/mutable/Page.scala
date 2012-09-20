package com.ibm.haploid

package core

package collection

package mutable

final class Page[T](

  val array: Array[T],

  val pageindex: Int)(

    implicit m: Manifest[T]) {

  def apply(index: Int) = array(index)

}

