package com.ibm.haploid

package dx

package engine

package domain

package test

import javax.xml.bind.annotation.XmlRootElement

import binding._

/**
 * Just for testing.
 */
@XmlRootElement(name = "job-nodetail")
case class JobNoDetail(@xmlElement(required = true) name: String)

  extends JobDetail {

  private def this() = this(null)

}

