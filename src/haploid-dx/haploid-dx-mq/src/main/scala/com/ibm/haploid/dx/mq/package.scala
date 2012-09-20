package com.ibm.haploid.dx

/**
 * This is just a template package.
 */
package object mq {
  import com.ibm.haploid.core.config._

  val hostname = getString("haploid.crt.mq.host")

  val port = getInt("haploid.crt.mq.port")

  val manager = getString("haploid.crt.mq.manager")

  val channel = getString("haploid.crt.mq.channel")

  val user = getString("haploid.crt.mq.user")

  val requestqueuename = getString("haploid.crt.mq.request-queue")

  val replyqueuename = getString("haploid.crt.mq.reply-queue")

  val timeout = getInt("haploid.crt.mq.timeout")

}