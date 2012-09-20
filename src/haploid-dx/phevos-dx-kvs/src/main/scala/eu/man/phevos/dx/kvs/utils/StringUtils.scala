package eu.man.phevos

package dx

package kvs

package utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

import eu.man.phevos.dx.kvs.KVSException

trait StringUtils {

  def replaceSpecialChars(str: String): String = str.replaceAll("[^0-9A-Z_]", "_")

  def mkLength(str: String, symbol: String, length: Int, ltr: Boolean = true, truncate: Boolean = false): String = try {
    if (str.length < length)
      if (ltr) {
        mkLength(str.concat(symbol), symbol, length, true, truncate)
      } else {
        mkLength(symbol.concat(str), symbol, length, false, truncate)
      }
    else if (truncate)
      str.substring(0, length)
    else
      str
  } catch {
    case e: Throwable ⇒
      throw KVSException(e.getMessage)
  }

  def getDateStringFromLong(time: Option[Long], pattern: String = "YYYYMMdd"): String = try {
    time match {
      case Some(l) ⇒
        getDateString(new Timestamp(l), pattern)
      case None ⇒
        getDateString(None, pattern)
    }
  } catch {
    case e: Throwable ⇒
      throw KVSException(e.getMessage)
  }

  def getDateString(timestamp: Option[Date], pattern: String): String = try {
    if (timestamp == None) {
      getDateString(new Date, pattern)
    } else {
      getDateString(timestamp.get, pattern)
    }
  } catch {
    case e: Throwable ⇒
      throw KVSException(e.getMessage)
  }

  private def getDateString(date: Date, pattern: String): String = try {
    val df = new SimpleDateFormat(pattern)
    df.format(date)
  } catch {
    case e: Throwable ⇒
      throw KVSException(e.getMessage)
  }

  def getTimestampFromString(s: String): Long = try {
    val timestring = (s.substring(0, 4) + "-" + s.substring(4, 6) + "-" + s.substring(6, 8) + " 00:00:00")
    Timestamp.valueOf(timestring).getTime
  } catch {
    case e: Throwable ⇒
      throw KVSException(e.getMessage)
  }

}