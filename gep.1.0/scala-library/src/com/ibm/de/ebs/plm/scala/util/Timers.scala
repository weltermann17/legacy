package com.ibm.de.ebs.plm.scala.util

object Timers {
  def now: Long = java.util.Calendar.getInstance.getTimeInMillis
  def time(f: => Unit) = { val begin = now; f; now - begin }
  object Unit {
    def millisecond: Long = 1L
    def second: Long = 1L * 1000
    def minute = 60 * second
    def hour = 60 * minute
    def day = 24 * hour
    def week = 7 * day
    def month = 30 * day
    def year = 365 * day
  }
  case class Value(value: Long) {
    def milliseconds = value
    def seconds = value * 1000
    def minutes = 60 * seconds
    def hours = 60 * minutes
    def days = 24 * hours
    def weeks = 7 * days
    def months = 30 * days
    def years = 365 * days
  }
  case class MilliSeconds(value: Long) {
    def inMilliSeconds = value
    def inSeconds = value / 1000.
    def inMinutes = inSeconds / 60
    def inHours = inMinutes / 60
    def inDays = inHours / 24
    def inWeeks = inDays / 7
    def inMonths = inDays / 30
    def inYears = inDays / 365
  }
  implicit def Int2Unit(value: Int) = { require(1 == value); Unit }
  implicit def Long2Unit(value: Long) = { require(1 == value); Unit }
  implicit def Int2Value(value: Int) = Value(value)
  implicit def Long2Value(value: Long) = Value(value)
  implicit def Long2MilliSeconds(value: Long) = MilliSeconds(value)
  val never = 0 seconds
  val forever = 99 years
}
