package com.ibm.de.ebs.plm.scala.rest

import org.restlet.routing.Router
import org.restlet.Restlet

class UriBuilder extends Iterator[(String, String => Restlet)] {
  builder =>

  def attach(router: Router) = result.foreach {
    case (uri, create) =>
      router.attach(uri, create(uri)); ()
  }
  override def hasNext = result.hasNext
  override def next = result.next
  private[rest] val paths = new scala.collection.mutable.ListBuffer[Path]
  private[rest] val templates = new scala.collection.mutable.ListBuffer[Template]
  private lazy val result =
    templates.toList.foldLeft(List[(String, String => Restlet)]()) { (l, t) => l ++ t.build }.sortWith((a, b) => a._1 < b._1).iterator
  protected implicit def String2Root(root: String) = Root(builder, root)
}

case class Root(builder: UriBuilder, rootstring: String) {
  root =>

  def ->(value: String) = Path(builder, root, value)
  def apply(value: String) = """\<\w+\>""".r.replaceFirstIn(rootstring, value)
}

case class Path(builder: UriBuilder, root: Root, value: String) {
  path =>

  val values = new scala.collection.mutable.ListBuffer[String]
  builder.paths += path
  values += value
  def +(value: String) = { values += value; path }
  def -->(template: String) = Template(builder, path, template)
  def parent: Option[Path] = {
    val index = builder.paths.toList.reverse.iterator.indexWhere(p => path == p) + 1
    builder.paths.toList.reverse.drop(index) match {
      case x :: xs => Some(x)
      case _ => None
    }
  }
  def build: List[(String, List[String])] = parent match {
    case Some(father) => for { (p, l) <- father.build; v <- values.toList } yield (p + root(v), v :: l)
    case None => for { v <- values.toList } yield (root(v), List[String](v))
  }
}

case class Template(builder: UriBuilder, path: Path, templatestring: String) {
  template =>

  val templates = new scala.collection.mutable.ListBuffer[String]
  val functions = new scala.collection.mutable.HashMap[String, List[String] => String => Restlet]
  builder.templates += template
  templates += templatestring
  def +(templatestring: String) = { templates += templatestring; template }
  def -->(f: List[String] => String => Restlet) = { functions += ((templates.last, f)); template }
  def build: List[(String, String => Restlet)] = for {
    (p, l) <- path.build
    t <- templates.toList
  } yield (p + t, functions(t)(l.reverse))
}
