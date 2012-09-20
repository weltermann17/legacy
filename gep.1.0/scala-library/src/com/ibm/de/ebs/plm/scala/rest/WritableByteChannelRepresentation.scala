package com.ibm.de.ebs.plm.scala.rest

import java.io.OutputStream
import java.nio.channels.Channels
import java.nio.channels.WritableByteChannel

import org.restlet.data.MediaType
import org.restlet.representation.WritableRepresentation

abstract class WritableByteChannelRepresentation(mediatype: MediaType)
  extends WritableRepresentation(mediatype) {

  override def isTransient = true
  override def getSize = -1
  override def getAvailableSize = -1

  override def write(out: OutputStream) {
    write(Channels.newChannel(out))
  }

  override def write(out: WritableByteChannel) {
    write(Channels.newOutputStream(out))
  }

}