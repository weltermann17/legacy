package com.ibm.de.ebs.plm.scala.database

import java.util.concurrent.ScheduledExecutorService

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters._
import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.annotations._
import org.squeryl.dsl._
import org.squeryl.dsl.ast._
import org.squeryl.internals._
import com.ibm.de.ebs.plm.scala.util.Timers.Int2Value

object SquerylHelpers {

  def initialize(connectionfactory: ConnectionFactory, adapter: DatabaseAdapter)(implicit scheduler: ScheduledExecutorService) = {
    SessionFactory.concreteFactory = Some(() =>
      Session.create(connectionfactory.newConnection(), adapter))
  }

  def hextoraw[A](s: StringExpression[A])(implicit m: OutMapper[A]): FunctionNode[A] with StringExpression[A] = {
    new FunctionNode("hextoraw", Some(m), Seq(s)) with StringExpression[A]
  }

  def rawtohex[A](s: StringExpression[A])(implicit m: OutMapper[A]): FunctionNode[A] with StringExpression[A] = {
    new FunctionNode("rawtohex", Some(m), Seq(s)) with StringExpression[A]
  }

  def raw[A](s: StringExpression[A])(implicit m: OutMapper[A]): FunctionNode[A] with StringExpression[A] = {
    new FunctionNode("utl_raw.cast_to_raw", Some(m), Seq(s)) with StringExpression[A]
  }

}