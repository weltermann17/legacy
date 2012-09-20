package com.ibm.de.ebs.plm.scala.database

abstract class InMemoryPreparedStatement extends java.sql.PreparedStatement {

  val resultset: java.sql.ResultSet

  def addBatch(sql: String) = ()
  def cancel = ()
  def clearBatch = ()
  def clearWarnings = ()
  def close = ()
  def execute(sql: String) = false
  def execute(sql: String, auto: Int) = false
  def execute(sql: String, cols: Array[Int]) = false
  def execute(sql: String, cols: Array[String]) = false
  def executeBatch: Array[Int] = null
  def executeQuery(sql: String): java.sql.ResultSet = resultset
  def executeUpdate(sql: String) = 0
  def executeUpdate(sql: String, auto: Int) = 0
  def executeUpdate(sql: String, cols: Array[Int]) = 0
  def executeUpdate(sql: String, cols: Array[String]) = 0
  def getConnection: java.sql.Connection = null
  def getFetchDirection = 0
  def getFetchSize = 0
  def getGeneratedKeys: java.sql.ResultSet = null
  def getMaxFieldSize = 0
  def getMaxRows = 0
  def getMoreResults = false
  def getMoreResults(i: Int) = false
  def getQueryTimeout = 0
  def getResultSet: java.sql.ResultSet = resultset
  def getResultSetConcurrency = 0
  def getResultSetHoldability = 0
  def getResultSetType = 0
  def getUpdateCount = 0
  def getWarnings: java.sql.SQLWarning = null
  def isClosed = false
  def isPoolable = false
  def setCursorName(name: String) = ()
  def setEscapeProcessing(enable: Boolean) = ()
  def setFetchDirection(d: Int) = ()
  def setFetchSize(size: Int) = ()
  def setMaxFieldSize(size: Int) = ()
  def setMaxRows(rows: Int) = ()
  def setPoolable(enable: Boolean) = ()
  def setQueryTimeout(t: Int) = ()
  def isWrapperFor(c: Class[_]) = false
  def unwrap[T](c: Class[T]): T = null.asInstanceOf[T]

  def addBatch = ()
  def clearParameters = ()
  def execute = false
  def executeQuery: java.sql.ResultSet = resultset
  def executeUpdate: Int = 0
  def getMetaData: java.sql.ResultSetMetaData = null
  def getParameterMetaData: java.sql.ParameterMetaData = null
  def setArray(i: Int, array: java.sql.Array) = ()
  def setAsciiStream(i: Int, in: java.io.InputStream) = ()
  def setAsciiStream(i: Int, in: java.io.InputStream, length: Int) = ()
  def setAsciiStream(i: Int, in: java.io.InputStream, length: Long) = ()
  def setBigDecimal(i: Int, big: java.math.BigDecimal) = ()
  def setBinaryStream(i: Int, in: java.io.InputStream) = ()
  def setBinaryStream(i: Int, in: java.io.InputStream, length: Int) = ()
  def setBinaryStream(i: Int, in: java.io.InputStream, length: Long) = ()
  def setBlob(i: Int, blob: java.sql.Blob) = ()
  def setBlob(i: Int, in: java.io.InputStream) = ()
  def setBlob(i: Int, in: java.io.InputStream, length: Long) = ()
  def setBoolean(i: Int, b: Boolean) = ()
  def setByte(i: Int, b: Byte) = ()
  def setBytes(i: Int, b: Array[Byte]) = ()
  def setCharacterStream(i: Int, in: java.io.Reader) = ()
  def setCharacterStream(i: Int, in: java.io.Reader, length: Int) = ()
  def setCharacterStream(i: Int, in: java.io.Reader, length: Long) = ()
  def setClob(i: Int, clob: java.sql.Clob) = ()
  def setClob(i: Int, in: java.io.Reader) = ()
  def setClob(i: Int, in: java.io.Reader, length: Long) = ()
  def setDate(i: Int, date: java.sql.Date) = ()
  def setDate(i: Int, date: java.sql.Date, cal: java.util.Calendar) = ()
  def setDouble(i: Int, d: Double) = ()
  def setFloat(i: Int, f: Float) = ()
  def setInt(i: Int, value: Int) = ()
  def setLong(i: Int, l: Long) = ()
  def setNCharacterStream(i: Int, in: java.io.Reader) = ()
  def setNCharacterStream(i: Int, in: java.io.Reader, length: Long) = ()
  def setNClob(i: Int, nclob: java.sql.NClob) = ()
  def setNClob(i: Int, in: java.io.Reader) = ()
  def setNClob(i: Int, in: java.io.Reader, length: Long) = ()
  def setNString(i: Int, s: String) = ()
  def setNull(i: Int, sqltype: Int) = ()
  def setNull(i: Int, sqltype: Int, typename: String) = ()
  def setObject(i: Int, o: Object) = ()
  def setObject(i: Int, o: Object, sqltype: Int) = ()
  def setObject(i: Int, o: Object, sqltype: Int, scale: Int) = ()
  def setRef(i: Int, ref: java.sql.Ref) = ()
  def setRowId(i: Int, rowid: java.sql.RowId) = ()
  def setShort(i: Int, s: Short) = ()
  def setSQLXML(i: Int, xml: java.sql.SQLXML) = ()
  def setString(i: Int, s: String) = ()
  def setTime(i: Int, t: java.sql.Time) = ()
  def setTime(i: Int, t: java.sql.Time, cal: java.util.Calendar) = ()
  def setTimestamp(i: Int, t: java.sql.Timestamp) = ()
  def setTimestamp(i: Int, t: java.sql.Timestamp, cal: java.util.Calendar) = ()
  def setUnicodeStream(i: Int, in: java.io.InputStream, length: Int) = ()
  def setURL(i: Int, url: java.net.URL) = ()

}

