/*
 * Copyright 2010 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.haploid

package core

package compiler

import java.io.File
import java.nio.file.Files.copy

import scala.io.Source.fromFile
import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.tools.nsc.io.Path.jfile2path
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualDirectory
import scala.tools.nsc.reporters.AbstractReporter
import scala.tools.nsc.util.BatchSourceFile
import scala.tools.nsc.Global
import scala.tools.nsc.Settings

import com.typesafe.config.ConfigFactory
import core.util.MD5
import core.logger

/**
 * A runtime compiler inspired by [com.twitter.util.Eval], but simplified a lot; each instance can only be used once to evaluate a T by calling 'apply'; the default constructor takes the code as a string.
 */

class Compiler(code: String, dir: File = null) {

  /**
   * Provide a list of source files and a directory to compile into; if the directory contains a class file that matches the given code then this file will be loaded; this is much(!) faster than newly compiling the code at runtime.
   */
  def this(sourcefiles: List[File], dir: File) = this(sourcefiles.map { fromFile(_).mkString }.mkString("\n"), dir)

  /**
   * Provide a sources files, the compiler will compile into the first file's parent directory.
   */
  def this(sourcefiles: List[File]) = this(sourcefiles, sourcefiles(0).getParentFile)

  /**
   * Convenience compiler for just one source file, the compiler will compile into this file's parent directory.
   */
  def this(sourcefile: File) = this(List(sourcefile), sourcefile.getParentFile)

  /**
   * Convenience compiler for just one source file, the compiler will compile into the given directory.
   */
  def this(sourcefile: File, dir: File) = this(List(sourcefile), dir)

  /**
   * Evaluate the provided code into an expression of type T, for example val i: Int = compiler()
   */
  def apply[T](): T = {
    val interpreter = new Interpreter
    if (directory.isDefined && classfile.exists) {
      interpreter.fromExisting
    } else {
      interpreter(wrappedcode)
    }
  }

  private class Interpreter {

    val outputdirectory = directory match {
      case Some(d) => AbstractFile.getDirectory(d)
      case None => new VirtualDirectory("virtual", None)
    }

    val settings = {
      val s = new Settings
      s.optimise.value = false
      s.deprecation.value = true
      s.unchecked.value = true
      s.outputDirs.setSingleOutput(outputdirectory)
      s.usejavacp.value = true
      val compath = Class.forName("scala.tools.nsc.Interpreter").getProtectionDomain.getCodeSource.getLocation
      val libpath = Class.forName("scala.Some").getProtectionDomain.getCodeSource.getLocation
      val haploidpath = Class.forName("com.ibm.haploid.core.concurrent.OnlyOnce").getProtectionDomain.getCodeSource.getLocation
      val akkapath = Class.forName("akka.actor.ActorSystem").getProtectionDomain.getCodeSource.getLocation
      val configpath = Class.forName("com.typesafe.config.ConfigFactory").getProtectionDomain.getCodeSource.getLocation
      s.bootclasspath.value = List(compath, libpath, haploidpath, akkapath, configpath, s.bootclasspath.value) mkString java.io.File.pathSeparator replace ("file:", "")
      s
    }

    val reporter = new AbstractReporter {
      def display(position: scala.tools.nsc.util.Position, message: String, severity: this.Severity) = {
        Compiler.this.error(severity + " " + message + " (" + position + ")")
      }
      def displayPrompt = ()
      val settings = Interpreter.this.settings
    }

    val global = new Global(settings, reporter)

    val classloader = new AbstractFileClassLoader(outputdirectory, this.getClass.getClassLoader)

    def apply[T](code: String) = {
	  if (config.getBoolean("haploid.config-scala.log-code-on-compile")) info(Compiler.this.code)
      val run = new global.Run
      val source = List(new BatchSourceFile("", code))
      run.compileSources(source)
      fromExisting[T]
    }

    def fromExisting[T] = {
      val c = classloader.loadClass(classname)
      c.getConstructor().newInstance().asInstanceOf[() => T].apply().asInstanceOf[T]
    }

    def copyResource(resource: String, directory: File) = {
      val in = getClass.getResourceAsStream(resource)
      val out = new File(core.file.temporaryDirectory, MD5(resource) + ".jar")
      copy(in, out.toPath)
      out.getAbsolutePath
    }

  }
  
  private def info(message: String) = {
    if (null != logger) logger.info(message) else print(message)
  }

  private def error(message: String) = {
    if (null != logger) logger.error(message) else println(message)
  }

  private val directory = dir match { case null => None case d => Some(d) }
  private lazy val md5 = MD5(code)
  private lazy val classname = "Compiler" + md5
  private lazy val classfile = directory.get.toPath.resolve(classname + ".class").toFile
  private lazy val wrappedcode = "class NAME extends (() => Any) { def apply = {\nCODE\n} }\n" replace ("NAME", classname) replace ("CODE", code)
  private lazy val config = ConfigFactory.load

}

