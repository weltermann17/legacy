package com.ibm.haploid

package dx

package engine

package domain

package test

import javax.xml.bind.annotation.{ XmlRootElement, XmlElement }

import operating.OperationDetail
import binding._

/**
 * Just for testing.
 */
@XmlRootElement(name = "operation-nodetail")
case class OperationNoDetail(

  @xmlAttribute(required = true) info: String)

  extends OperationDetail("noop") {

  private def this() = this(null)

}

