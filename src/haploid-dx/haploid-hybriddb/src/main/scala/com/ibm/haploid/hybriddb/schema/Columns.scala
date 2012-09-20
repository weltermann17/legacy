package com.ibm.haploid

package hybriddb

package schema

import core.reflect.properties

trait Columns extends Map[String, Any] with Serializable 
//{
//
//  val length: Int
//
//  def get(key: String): Option[Column[_]] = columns.get(key)
//
//  def iterator = columns.iterator
//
//  def +[B1 >: Column[_]](kv: (String, B1)) = this
//  
//  def -(key: String) = this
//  
//  override def size = columns.size
//  
//  @transient lazy val columns = properties[Column[_]](this)
//
// // @transient lazy val filled = columns.values.foreach(_.filled)
//
//  @transient implicit protected[this] val _length = length
//
//}

