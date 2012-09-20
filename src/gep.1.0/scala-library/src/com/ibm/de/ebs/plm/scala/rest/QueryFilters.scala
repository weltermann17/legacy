package com.ibm.de.ebs.plm.scala.rest

import scala.util.parsing.combinator.RegexParsers

import org.restlet.routing.Filter
import org.restlet.Restlet
import org.restlet.Context
import org.restlet.Request
import org.restlet.Response

import com.ibm.de.ebs.plm.scala.parsing.combinator.ProcessResult
import com.ibm.de.ebs.plm.scala.text.StringConversions.isNumber

/* examples:
 * ?not&and&like&vid&XX%2A&inlist&vdesc&4711%2C3.141592&sort&vid&sortorder&desc&from&1&to&20
 * ?or&not&and&like&vid&XX*&inlist&vdesc&4711%2C3.141592&lowerthan&lastmodified&now-60&
 * */

trait CanQuery {
  this: Restlet =>
  val queryparameternames = new scala.collection.mutable.ListBuffer[String]
}

abstract class QueryFilter(next: Restlet with CanQuery, context: Context) extends Filter(context, next) {

  override protected def beforeHandle(request: Request, response: Response) = {
    val attributes = request.getAttributes
    try {
      if (request.getResourceRef.hasQuery) QueryParser(request.getResourceRef.getQuery(true)).parse.foreach {
        case (n, v) =>
          next.queryparameternames += n
          attributes.put(n, v)
      }
    } catch {
      case e => context.getLogger.severe("QueryFilter: " + e); e.printStackTrace
    }
    Filter.CONTINUE
  }

  private trait HelperParsers extends RegexParsers {
    def positiveinteger = """[0-9]\d*""".r
    def positivelong = """[0-9]\d*""".r
    def ident = """\w+""".r ^^ { id => mapColumns(id) }
    private val idents = new scala.collection.mutable.ListBuffer[String]
  }

  private type Result = Map[String, String]
  private val pWhere = "com.ibm.de.ebs.plm.scala.rest.query.where"
  private val pOrderby = "com.ibm.de.ebs.plm.scala.rest.query.orderby"
  private val pFrom = "com.ibm.de.ebs.plm.scala.rest.query.from"
  private val pTo = "com.ibm.de.ebs.plm.scala.rest.query.to"

  private case class QueryParser(in: String) extends HelperParsers with ProcessResult[Result, List[(String, String)]] {
    def parse = process(parseAll(query, in)) { e =>
      context.getLogger.fine(e.toList.toString)
      e.toList
    }
    def query: Parser[Result] = where ~ opt(orderby) ~ opt(range) ~ opt(token) <~ opt("&") ^^ {
      case w ~ Some(o) ~ Some((f, t)) ~ Some(token) => Map(pWhere -> w, pOrderby -> o.toString, pFrom -> f.toString, pTo -> t.toString)
      case w ~ Some(o) ~ None ~ Some(token) => Map(pWhere -> w, pOrderby -> o.toString, pFrom -> "0", pTo -> "0")
      case w ~ None ~ Some((f, t)) ~ Some(token) => Map(pWhere -> w, pFrom -> f.toString, pTo -> t.toString)
      case w ~ None ~ None ~ Some(token) => Map(pWhere -> w, pOrderby -> "", pFrom -> "0", pTo -> "500")
      case w ~ Some(o) ~ Some((f, t)) ~ None => Map(pWhere -> w, pOrderby -> o.toString, pFrom -> f.toString, pTo -> t.toString)
      case w ~ Some(o) ~ None ~ None => Map(pWhere -> w, pOrderby -> o.toString, pFrom -> "0", pTo -> "0")
      case w ~ None ~ Some((f, t)) ~ None => Map(pWhere -> w, pFrom -> f.toString, pTo -> t.toString)
      case w ~ None ~ None ~ None => Map(pWhere -> w, pOrderby -> "", pFrom -> "0", pTo -> "500")
    }
    def where: Parser[String] = expr ^^ { e => prefixWhere + e.toString + suffixWhere }
    def expr: Parser[Expr] = (unary | binary | conditional)
    def unary: Parser[Expr] = "not&" ~> expr ^^ { e => Not(e) }
    def binary: Parser[Expr] = ("and" | "or") ~ "&" ~ expr ~ expr ^^ { case op ~ "&" ~ l ~ r => Binary(op, l, r) }
    def conditional: Parser[Expr] = ("equal" | "notequal" | "lowerthan" | "greaterthan" |
      "lowerorequal" | "greaterorequal" | "like" | "inlist" | "between") ~ "&" ~ attribute ^^
      { case op ~ "&" ~ a => Condition(Op(op), a) }
    def attribute: Parser[Attribute] = name ~ "&" ~ value ^^ { case n ~ "&" ~ v => Attribute(n, v) }
    def name: Parser[String] = ident
    def value: Parser[Value] = repsep("""^(?:(?!,)[^&])*""".r, ",") ^^ { case v => Value(v) }
    def orderby: Parser[Expr] = sort ~ opt(sortorder) ^^ { case s ~ o => Sort(s, o) }
    def sort: Parser[String] = "&sort&" ~> name
    def sortorder: Parser[Boolean] = "&sortorder&" ~> ("desc" | "asc") ^^ { case "asc" => true case "desc" => false }
    def range: Parser[(Int, Int)] = from ~ to ^^ { case f ~ t => (scala.math.min(f, t), scala.math.max(f, t)) }
    def from: Parser[Int] = "&from=" ~> positiveinteger <~ "&" ^^ { i => i.toInt }
    def to: Parser[Int] = "to=" ~> positiveinteger ^^ { i => i.toInt }
    def token: Parser[Long] = "&token=" ~> positivelong ^^ { l => l.toLong }
    abstract sealed class Expr {
      val s: String
      override def toString = s
    }
    case class Not(e: Expr) extends Expr {
      val s = "not(" + e.toString + ")"
    }
    case class Binary(op: String, left: Expr, right: Expr) extends Expr {
      val s = "(" + left.toString + ")" + op + "(" + right.toString + ")"
    }
    case class Condition(op: Op, attr: Attribute) extends Expr {
      val s = attr.name + op + (op.toString match {
        case " like " => LikeParser(attr.value(0)).parse
        case " between " => attr.value(0) + " and " + attr.value(1)
        case " in " => "[" + attr.value.asCommaSeparated + "]"
        case _ => attr.value(0)
      })
    }
    case class Op(op: String) extends Expr {
      val s = op match {
        case "equal" => "="
        case "notequal" => "!="
        case "lowerthan" => "<"
        case "greaterthan" => ">"
        case "lowerorequal" => "<="
        case "greaterorequal" => ">="
        case "like" => " like "
        case "inlist" => " in "
        case "between" => " between "
      }
    }
    case class Attribute(name: String, value: Value) {
    }
    case class Value(list: List[String]) {
      require(!list.exists(v => v.contains("--") || v.contains("/*") || v.contains("{")))
      def apply(i: Int) = values(i)
      def asCommaSeparated = values.foldLeft("") { (l, v) => val comma = if (0 < l.length) "," else ""; l + comma + v }
      val values = list.map { v => if (isNumber(v)) v else OtherValue(v).parse }
      case class OtherValue(value: String) extends HelperParsers with ProcessResult[String, String] {
        def parse = process(parseAll(othervalue, value)) { v => v }
        def othervalue = now | today | raw | string
        def now = "now" ~> int ^^ { nowPlusMinutes }
        def today = "today" ~> int ^^ { todayPlusDays }
        def int = opt(("+" | "-") ~ positiveinteger) ^^ { case Some("-" ~ i) => -i.toInt case Some("+" ~ i) => i.toInt case None => 0 }
        def raw = "0x" ~> string ^^ { hextoraw }
        def string = rep(".".r) ^^ { "'" + flatten(_) + "'" }
      }
    }
    case class LikeParser(value: String) extends RegexParsers with ProcessResult[String, String] {
      def parse = process(parseAll(like, value)) { v => v + (if (escaped) " escape '!'" else "") }
      def like = rep(tokens) ^^ { flatten }
      def tokens = star | escapedstar | percent | underscore | any
      def star = "*" ^^ { _ => "%" }
      def escapedstar = """\*""" ^^ { _ => "*" }
      def percent = "%" ^^ { _ => escaped = true; "!%" }
      def underscore = "_" ^^ { _ => escaped = true; "!_" }
      def any = ".".r
      var escaped = false
    }
    case class Sort(attribute: String, order: Option[Boolean]) extends Expr {
      val s = " " + attribute + (order match { case Some(o) => if (o) " asc" else " desc" case _ => " asc" })
    }
    def flatten(l: List[String]) = l.foldLeft("") { (l, c) => l + c }
  }
  protected def nowPlusMinutes(minutes: Int): String
  protected def todayPlusDays(days: Int): String
  protected def hextoraw(hex: String): String
  protected def mapColumns(name: String): String
  protected val prefixWhere: String = "("
  protected val suffixWhere: String = ")"
}

abstract class OracleQueryFilter(next: Restlet with CanQuery, context: Context) extends QueryFilter(next, context) {

  protected def nowPlusMinutes(minutes: Int) = {
    val delta = ((1. / (24 * 60)) * minutes).toFloat
    "sysdate" + (if (0 == delta) "" else (if (0 > delta) "" else "+") + delta)
  }
  protected def todayPlusDays(days: Int) = "sysdate" + (if (0 > days) "" else "+") + days
  protected def hextoraw(hex: String) = "hextoraw(" + hex + ")"
}

