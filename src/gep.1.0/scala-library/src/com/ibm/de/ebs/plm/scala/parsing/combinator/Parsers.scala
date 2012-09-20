package com.ibm.de.ebs.plm.scala.parsing.combinator

import scala.util.parsing.combinator.Parsers

trait ProcessResult[T, S] extends Parsers {
  def process(parseresult: ParseResult[T])(p: T => S) = parseresult match {
    case Success(result, _) => p(result)
    case e @ Failure(msg, _) => throw new Exception(e.toString)
    case e @ Error(msg, _) => throw new Exception(e.toString)
  }
  def processShort(parseresult: ParseResult[T])(p: T => S) = parseresult match {
    case Success(result, _) => p(result)
    case e @ Failure(_, _) => throw new Exception(e.toString.substring(0, 256))
    case e @ Error(_, _) => throw new Exception(e.toString.substring(0, 256))
  }
}