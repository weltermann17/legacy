package com.ibm.haploid

package core

package util

import java.io.FileOutputStream
import java.lang.management.ManagementFactory
import java.util.regex.Pattern

import com.sun.jna.Pointer

import scala.collection.JavaConversions.seqAsJavaList

import core.file.{ temporaryDirectory, deleteDirectory }
import core.util.io.copyBytes

package object process {

  /**
   * Gets the process id of the running JVM.
   */
  def getCurrentProcessId = {
    val pattern = Pattern.compile("^([0-9]+)@.+$", Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(ManagementFactory.getRuntimeMXBean.getName)
    if (matcher.matches) Integer.parseInt(matcher.group(1)) else throw new NoSuchMethodError("Could not get current JVM process id.")
  }

  /**
   * Gets the process id of the given process.
   */
  def getProcessId(process: Process): Int = {
    if (process.getClass.getName == "java.lang.UNIXProcess") {
      val f = process.getClass.getDeclaredField("pid")
      f.setAccessible(true)
      f.getInt(process)
    } else if (List("java.lang.Win32Process", "java.lang.ProcessImpl").contains(process.getClass.getName)) {
      val f = process.getClass.getDeclaredField("handle")
      f.setAccessible(true)
      val handl = f.getLong(process)
      val kernel = Kernel32.INSTANCE
      val handle = new W32API.HANDLE
      handle.setPointer(Pointer.createConstant(handl))
      kernel.GetProcessId(handle)
    } else {
      throw new NoSuchMethodError("getProcessId : Not implemented for this class : " + process.getClass.getName)
    }
  }

  /**
   *
   */
  def killProcessWithChildren(process: Process) = {
    val currentid = getCurrentProcessId
    val processid = getProcessId(process)
    operatingsystem match {
      case "windows" ⇒
        val tempdir = temporaryDirectory
        val pskill = tempdir.toPath.resolve("pskill.exe")
        val out = new FileOutputStream(pskill.toFile)
        copyBytes(getClass.getResourceAsStream("/pskill.exe"), out)
        out.close
        val commands = List(pskill.toAbsolutePath.toString, "-t", processid.toString)
        core.logger.debug("killProcessWithChildren : " + commands)
        new ProcessBuilder(commands).start.waitFor match {
          case 0 ⇒ core.logger.debug("killProcessWithChildren : Killed process and all of its children : " + processid)
          case c ⇒ core.logger.error("killProcessWithChildren : Could not kill process : " + processid)
        }
        deleteDirectory(tempdir)
      // case "unix" ⇒
      case _ ⇒ throw new NoSuchMethodError("killProcessWithChildren : Not implemented for this operating system.")
    }
  }

  /**
   *
   */
  def killNamedProcessWithChildren(processname: String) = {
    operatingsystem match {
      case "windows" ⇒
        val tempdir = temporaryDirectory
        val pskill = tempdir.toPath.resolve("pskill.exe")
        val out = new FileOutputStream(pskill.toFile)
        copyBytes(getClass.getResourceAsStream("/pskill.exe"), out)
        out.close
        val commands = List(pskill.toAbsolutePath.toString, "-t", processname)
        core.logger.debug("killNamedProcessWithChildren : " + commands)
        new ProcessBuilder(commands).start.waitFor match {
          case 0 ⇒ core.logger.debug("killNamedProcessWithChildren : Killed process and all of its children : " + processname)
          case c ⇒ core.logger.debug("killNamedProcessWithChildren : Process not found : " + processname)
        }
        deleteDirectory(tempdir)
      // case "unix" ⇒
      case _ ⇒ throw new NoSuchMethodError("killNamedProcessWithChildren : Not implemented for this operating system.")
    }

  }
}
