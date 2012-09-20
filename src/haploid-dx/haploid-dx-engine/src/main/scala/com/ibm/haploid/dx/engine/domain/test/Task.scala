package com.ibm.haploid

package dx

package engine

package domain

package test

import javax.xml.bind.annotation.XmlRootElement

import binding.xmlAttribute

/**
 * Just for testing.
 */
@XmlRootElement(name = "task-nodetail")
case class TaskNoDetail(

  @xmlAttribute(required = true) info: String)

  extends TaskDetail {

  private def this() = this(null)

}

