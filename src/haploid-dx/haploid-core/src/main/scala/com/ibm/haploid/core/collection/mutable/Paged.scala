package com.ibm.haploid

package core

package collection

package mutable

import caching.LruCache

trait Paged[T] {

  def get(index: Int) = page(index)(index % entriesperpage)

  def update(index: Int, elem: T) = throw new UnsupportedOperationException

  val pagesize: Int

  val entriesperpage: Int

  val numberofpages: Int

  protected[this] def loadPage(pageindex: Int): Page[T]

  private[this] def page(index: Int): Page[T] = {
    val pageindex = index / entriesperpage
    cache.get(pageindex) match {
      case Some(page) => page
      case _ => loadPage(pageindex) match { case page => cache.add(pageindex, page); page }
    }
  }

  logger.debug("numberofpages " + numberofpages + ", pagesize " + pagesize + ", entriesperpage " + entriesperpage)

  private[this] val cache = new LruCache[Page[T]]

}

