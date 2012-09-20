package com.ibm.haploid

package core

import java.io.{ IOException, File }
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConversions.collectionAsScalaIterable

import org.apache.commons.io.FileUtils

import core.config.{ getMilliseconds, getInt }

/**
 * File utilities.
 */
package object file {

  import java.io.File
  import java.io.RandomAccessFile
  import java.nio.file.Files
  import java.nio.channels.FileChannel
  import java.util.concurrent.ConcurrentHashMap

  import scala.collection.JavaConversions._

  import core.config._

  val deletedirectoryretries = getInt("haploid.core.file.delete-directory-retries")

  val deletedirectorytimeout = getMilliseconds("haploid.core.file.delete-directory-timeout")

  /**
   * Create a temporary file somewhere in the default location. It will be deleted at JVM shutdown.
   */
  def temporaryFile = {
    val f = Files.createTempFile(null, null).toFile
    deleteOnExit(f)
    f
  }

  /**
   * Create a temporary file in the given directory. It will be deleted at JVM shutdown.
   */
  def temporaryFileInDirectory(directory: File) = {
    val f = Files.createTempFile(directory.toPath, null, null).toFile
    deleteOnExit(f)
    f
  }

  /**
   * Create a temporary directory somewhere in the default location. It will be deleted at JVM shutdown together with all files it includes.
   */
  def temporaryDirectory = {
    val d = Files.createTempDirectory(null).toFile
    deleteOnExit(d)
    d
  }

  /**
   * Delete a directory and all of its contents. Use delete-directory-retries and delete-directory-timeout to make this method more robust.
   */
  def deleteDirectory(directory: File) = try {
    if (directory.exists) FileUtils.deleteDirectory(directory)
  } catch {
    case e: IOException ⇒
      var retries = deletedirectoryretries
      while (0 < retries) {
        try {
          Thread.sleep(deletedirectorytimeout)
          FileUtils.deleteDirectory(directory)
          retries = 0
        } catch {
          case e: IOException ⇒ retries -= 1
        }
      }
    case e: Throwable ⇒ core.logger.debug("Could not delete directory : " + e)
  }

  /**
   * This file will be automatically deleted at JVM shutdown.
   */
  def deleteOnExit(file: File) = DeleteOnExit.add(file)

  private object DeleteOnExit {

    def add(file: File) = files.put(file.getAbsolutePath, file)

    def delete = {
      files.values.filter(!_.isDirectory).foreach(_.delete)
      files.values.filter(_.isDirectory).foreach { d ⇒ d.listFiles.foreach(_.delete); d.delete }
    }

    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable { def run = delete }))

    private[this] val files = new ConcurrentHashMap[String, File]

  }

}
