package com.ibm.de.ebs.plm.scala.database

/**
 * see: http://scala.sygneca.com/code/simplifying-jdbc
 *
 */

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Time
import java.sql.Timestamp
import java.sql.Types

object ConnectionHelpers {

  implicit def conn2Statement(conn: Connection): Statement = conn.createStatement

  implicit def rrs2Boolean(rs: RichResultSet) = rs.nextBoolean
  implicit def rrs2Byte(rs: RichResultSet) = rs.nextByte
  implicit def rrs2Int(rs: RichResultSet) = rs.nextInt
  implicit def rrs2Long(rs: RichResultSet) = rs.nextLong
  implicit def rrs2Float(rs: RichResultSet) = rs.nextFloat
  implicit def rrs2Double(rs: RichResultSet) = rs.nextDouble
  implicit def rrs2String(rs: RichResultSet) = rs.nextString
  implicit def rrs2NString(rs: RichResultSet) = rs.nextNString
  implicit def rrs2Date(rs: RichResultSet) = rs.nextDate
  implicit def rrs2Time(rs: RichResultSet) = rs.nextTime
  implicit def rrs2Timestamp(rs: RichResultSet) = rs.nextTimestamp

  implicit def rs2Boolean(rs: RichResultSet) = rs.nextBoolean match { case Some(b) => b case _ => false }
  implicit def rs2Byte(rs: RichResultSet): Byte = rs.nextByte match { case Some(b) => b case _ => 0 }
  implicit def rs2Int(rs: RichResultSet): Int = rs.nextInt match { case Some(i) => i case _ => 0 }
  implicit def rs2Long(rs: RichResultSet): Long = rs.nextLong match { case Some(l) => l case _ => 0 }
  implicit def rs2Float(rs: RichResultSet) = rs.nextFloat match { case Some(f) => f case _ => 0.f }
  implicit def rs2Double(rs: RichResultSet) = rs.nextDouble match { case Some(d) => d case _ => 0. }
  implicit def rs2String(rs: RichResultSet) = rs.nextString match { case Some(s) => s case _ => "" }
  implicit def rs2NString(rs: RichResultSet): NString = rs.nextNString match { case Some(s) => s case _ => new NString("") }
  implicit def rs2Date(rs: RichResultSet) = rs.nextDate match { case Some(d) => d case _ => Date.valueOf("1970-01-01") }
  implicit def rs2Time(rs: RichResultSet) = rs.nextTime match { case Some(t) => t case _ => Time.valueOf("00:00:00") }
  implicit def rs2Timestamp(rs: RichResultSet) = rs.nextTimestamp match { case Some(t) => t case _ => Timestamp.valueOf("1970-01-01 00:00:00.000000000") }

  implicit def resultSet2Rich(rs: ResultSet) = new RichResultSet(rs)
  implicit def rich2ResultSet(r: RichResultSet) = r.rs

  implicit def ps2Rich(ps: PreparedStatement) = new RichPreparedStatement(ps)
  implicit def rich2PS(r: RichPreparedStatement) = r.ps

  implicit def str2RichPrepared(s: String)(implicit conn: Connection): RichPreparedStatement = conn.prepareStatement(s)
  implicit def conn2Rich(conn: Connection) = new RichConnection(conn)

  implicit def st2Rich(s: Statement) = new RichStatement(s)
  implicit def rich2St(rs: RichStatement) = rs.s

  class NString(val s: String) extends Serializable {
    override def toString = s
    override def equals(other: Any) = s.equals(other.asInstanceOf[NString].s)
    override def hashCode = s.hashCode
  }

  implicit def nstring2s(ns: NString) = ns.s

  implicit def s2nstring(s: String) = new NString(s)

  class RichResultSet(val rs: ResultSet) {
    var pos = 1

    def apply(i: Int) = { pos = i; this }

    def nextBoolean: Option[Boolean] = { val ret = rs.getBoolean(pos); pos += 1; if (rs.wasNull) None else Some(ret) }
    def nextByte: Option[Byte] = { val ret = rs.getByte(pos); pos += 1; if (rs.wasNull) None else Some(ret) }
    def nextInt: Option[Int] = { val ret = rs.getInt(pos); pos += 1; if (rs.wasNull) None else Some(ret) }
    def nextLong: Option[Long] = { val ret = rs.getLong(pos); pos += 1; if (rs.wasNull) None else Some(ret) }
    def nextFloat: Option[Float] = { val ret = rs.getFloat(pos); pos += 1; if (rs.wasNull) None else Some(ret) }
    def nextDouble: Option[Double] = { val ret = rs.getDouble(pos); pos += 1; if (rs.wasNull) None else Some(ret) }
    def nextString: Option[String] = { val ret = rs.getString(pos); pos += 1; if (rs.wasNull) None else Some(ret) }
    def nextNString: Option[NString] = {
      val b = rs.getBytes(pos)
      val ret = if (null == b) {
        null
      } else {
        (0 until b.length).foreach { i => if (0 < b(i) && b(i) < 32) b.update(i, '.') }
        new String(b, "ISO-8859-1")
      }
      pos += 1
      if (rs.wasNull) None else Some(new NString(ret))
    }
    def nextDate: Option[Date] = { val ret = rs.getDate(pos); pos += 1; if (rs.wasNull) None else Some(ret) }
    def nextTime: Option[Time] = { val ret = rs.getTime(pos); pos += 1; if (rs.wasNull) None else Some(ret) }
    def nextTimestamp: Option[Timestamp] = { val ret = rs.getTimestamp(pos); pos += 1; if (rs.wasNull) None else Some(ret) }

    def foldLeft[X](init: X)(f: (ResultSet, X) => X): X = rs.next match {
      case false => init
      case true => foldLeft(f(rs, init))(f)
    }
    def map[X](f: ResultSet => X) = {
      var ret = List[X]()
      while (rs.next()) {
        ret = f(rs) :: ret
      }
      ret.reverse
    }
  }

  class RichPreparedStatement(val ps: PreparedStatement) {
    var pos = 1
    var repeat = 1
    private def inc = { pos += 1; this }

    def execute[X](f: RichResultSet => X): Stream[X] = {
      pos = 1
      makestream(f, ps.executeQuery)
    }
    def <<![X](f: RichResultSet => X): Stream[X] = execute(f)

    def execute = { pos = 1; ps.execute }
    def <<! = execute

    def <<(x: Option[Any]): RichPreparedStatement = {
      x match {
        case None =>
          ps.setNull(pos, Types.NULL)
          inc
        case Some(y) => (this << y)
      }
    }
    def <<?(n: Int): RichPreparedStatement = {
      repeat = n
      this
    }
    def <<(x: Any): RichPreparedStatement = {
      while (0 < repeat) {
        x match {
          case z: Boolean =>
            ps.setBoolean(pos, z)
          case z: Byte =>
            ps.setByte(pos, z)
          case z: Int =>
            ps.setInt(pos, z)
          case z: Long =>
            ps.setLong(pos, z)
          case z: Float =>
            ps.setFloat(pos, z)
          case z: Double =>
            ps.setDouble(pos, z)
          case z: String =>
            ps.setString(pos, z)
          case z: Date =>
            ps.setDate(pos, z)
          case z => ps.setObject(pos, z)
        }
        inc
        repeat -= 1
      }
      this <<? 1
    }
  }

  class RichConnection(val conn: Connection) {
    def <<(sql: String) = new RichStatement(conn.createStatement) << sql
    def <<(sql: Seq[String]) = new RichStatement(conn.createStatement) << sql
  }

  class RichStatement(val s: Statement) {
    def <<(sql: String) = { s.execute(sql); this }
    def <<(sql: Seq[String]) = { for (x <- sql) s.execute(x); this }
  }

  private def makestream[X](f: RichResultSet => X, rs: ResultSet): Stream[X] = {
    if (rs.next) {
      Stream.cons(f(new RichResultSet(rs)), makestream(f, rs))
    } else {
      rs.close
      Stream.empty
    }
  }

  implicit def query[X](s: String, f: RichResultSet => X)(implicit stat: Statement): Stream[X] = {
    makestream(f, stat.executeQuery(s))
  }

  def iso8601(timestamp: java.sql.Timestamp) = {
    val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    format.format(timestamp)
  }

}
