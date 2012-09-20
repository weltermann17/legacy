package com.ibm.haploid

package hybriddb

package io

import java.io.{ File, ObjectOutputStream, BufferedOutputStream, FileOutputStream }
import java.util.zip.GZIPOutputStream

import core.logger
import core.dispose._

import schema._

class Serializer[C <: Columns](file: File) extends Extractor[C, Unit] {

  def extract(table: Table[C]) = {
    try {
      using {
        implicit val _ = forceContextType[Unit]
        val out = disposable(
          new ObjectOutputStream(
            new BufferedOutputStream(
              new GZIPOutputStream(
                new FileOutputStream(file), buffersize) {
                `def`.setLevel(java.util.zip.Deflater.BEST_SPEED)
              }, buffersize)))
        out.writeObject(table)
        logger.info("Serialized ok " + table.columns.getClass.getSimpleName)
      }
    } catch {
      case e =>        
        logger.warning("Serialize failed for " + table.columns.getClass.getName + " to " + file + " (" + e + ").")
        file.delete
        throw e
    }
  }

}

