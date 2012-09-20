package com.ibm.haploid

package hybriddb

package io

import java.io.{ File, ObjectInputStream, BufferedInputStream, FileInputStream, FileNotFoundException }
import java.util.zip.GZIPInputStream

import core.logger
import core.dispose._

import schema._

class Deserializer[C <: Columns](file: File)(implicit manifest: Manifest[C]) extends Filler {

  def fill: Table[C] = {
    try {
      using {
        implicit val _ = forceContextType[Table[C]]
        if (!file.exists) throw new FileNotFoundException(file.getAbsolutePath)
        val in = disposable(
          new ObjectInputStream(
            new BufferedInputStream(
              new GZIPInputStream(
                new FileInputStream(file),
                buffersize),
              buffersize)))
        val table = in.readObject.asInstanceOf[Table[C]]
//        table.columns.filled
        logger.info("Deserialized ok " + table.columns.getClass.getSimpleName)
        table
      }
    } catch {
      case e =>
        logger.warning("Deserialize failed for " + manifest.erasure.getName + " from " + file + " (" + e + ").")
        file.delete
        throw e
    }
  }

}

