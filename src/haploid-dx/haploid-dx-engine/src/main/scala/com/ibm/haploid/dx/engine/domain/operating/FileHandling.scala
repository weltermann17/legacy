package com.ibm.haploid

package dx

package engine

package domain

package operating

import java.io.{ InputStreamReader, FileWriter, FileOutputStream }
import java.nio.file.{ StandardCopyOption, Path, Files }

import com.ibm.haploid.core.service.Result

import core.file.deleteDirectory
import core.service.{ Success, Result, Failure }
import core.util.io.{ copyLines, copyBytes }

/**
 *
 */
trait FileHandler {

  self: OperatorBase with Local ⇒

  val basedirectory: Path

  val workingdirectory = basedirectory.resolve("work")

  val outputdirectory = basedirectory.resolve("out")

  resetDirectories

  def resetDirectories = {
    try {
      deleteDirectory(workingdirectory.toFile)
      deleteDirectory(outputdirectory.toFile)
      Files.createDirectories(workingdirectory)
      Files.createDirectories(outputdirectory)
    } catch {
      case e: Throwable ⇒
        core.newLogger(this).error("Cannot create operating environment : " + e)
    }
  }

  private def doFileOperation(
    operation: (Path, Path) ⇒ Path,
    targetDir: Path): Path ⇒ Result[Path] = {
    source ⇒
      try {
        val target = targetDir.resolve(source.getFileName).toAbsolutePath
        Files.createDirectories(target.getParent)
        debug("File operation (from, to) : " + source + " -> " + target)
        Success(operation(source, target).toAbsolutePath)
      } catch {
        case e: Exception ⇒ Failure(e)
      }
  }

  private def doFileOperations(operation: Path ⇒ Result[Path]): List[Path] ⇒ Result[List[Path]] = {
    sources ⇒
      try {
        var outPaths: List[Path] = List.empty
        sources.foreach { source ⇒
          val result = operation(source)
          result match {
            case Success(targetSource) ⇒
              outPaths = outPaths :+ targetSource
            case Failure(e) ⇒
              throw e
          }
        }
        Success(outPaths)
      } catch {
        case e: Throwable ⇒
          Failure(e)
      }
  }

  def copyFileToWorkingDirectory(source: Path) = {
    doFileOperation(((from, to) ⇒ Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING)), workingdirectory)(source)
  }

  def copyBinaryResourceToWorkingDirectory(resource: Path) = {
    doFileOperation(((from, to) ⇒ {
      val filename = from.getFileName
      val out = new FileOutputStream(to.toFile)
      copyBytes(getClass.getResourceAsStream("/" + from), out)
      out.close
      to
    }), workingdirectory)(resource)
  }

  def copyTextResourceToWorkingDirectory(resource: Path) = {
    doFileOperation(((from, to) ⇒ {
      val filename = from.getFileName
      val out = new FileWriter(to.toFile)
      copyLines(new InputStreamReader(getClass.getResourceAsStream("/" + from), "UTF-8"), out)
      out.close
      to
    }), workingdirectory)(resource)
  }

  def moveFileToOutputDirectory(source: Path) = {
    doFileOperation(((from, to) ⇒ Files.move(from, to, StandardCopyOption.REPLACE_EXISTING)), outputdirectory)(source)
  }

  def copyToWorkingDirectory(sources: Path*) = doFileOperations(copyFileToWorkingDirectory)(sources.toList)

  def copyBinaryResourcesToWorkingDirectory(resources: Path*) = doFileOperations(copyBinaryResourceToWorkingDirectory)(resources.toList)

  def copyTextResourcesToWorkingDirectory(resources: Path*) = doFileOperations(copyTextResourceToWorkingDirectory)(resources.toList)

  def moveToOutputDirectory(sources: Path*) = doFileOperations(moveFileToOutputDirectory)(sources.toList)

  def renameFile(current: Path, newname: String): Result[Path] = {
    try {
      val target = current.resolveSibling(newname)
      debug("Renaming file : " + current + " -> " + target)
      Success(Files.move(current, target))
    } catch {
      case e: Throwable ⇒
        Failure(e)
    }
  }

  def renameFile(current: String, newname: String): Result[Path] = {
    try {
      debug("Renaming working file : " + current + " -> " + newname)
      renameFile(workingdirectory.resolve(current), newname)
    } catch {
      case e: Throwable ⇒
        Failure(e)
    }
  }

}

