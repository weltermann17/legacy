package com.ibm.haploid.hybriddb.schema.column
import collection.SortedMap
import scala.util.control.Breaks._

trait Searchable extends Column[String] {

  protected[this] def representation: SortedMap[String, Int]

  def search(term: String, max: Int = 0, start: Int = 0): Option[collection.Set[Int]] = {
    require(start >= 0 && start < representation.size, "Start index out of bound.")

    val t = term.trim()

    if (!t.contains("*")) {
      val result = representation.get(t)
      if (result != None) return Some(Set(result.get))
    } else if (t.endsWith("*")) {
      // TODO: Create Sublist
      return regexsearch(representation, t) // TODO return sublist instead of regexsearch
    } else if (t.startsWith("*")) {
      return regexsearch(representation, t)
    } else {
    	// TODO: Create Sublist
      return regexsearch(representation, t) // TODO Use sublist instead of representation
    }

    return None
  }

  private def regexsearch(list: SortedMap[String, Int], term: String, max: Int = 0): Option[collection.Set[Int]] = {
    val t = {
      var res = term

      List("\\", "[", "^", "$", ".", "|", "?", "+", "(", ")", "{", "}").foreach(s =>
        res = res.replace(s, "\\" + s))

      res.replace("*", ".*")
    }

    val set = new collection.mutable.BitSet

    breakable {
      list.foreach({
        case (k, v) =>
          if (k.matches(t)) set += v
          if (max > 0 && set.size >= max) break
      })
    }
    
    if (set.size > 0)
      Some(set)
    else
      return None

  }

}