package com.ibm.de.ebs.plm.scala.caching

trait Caching[K, V] {
  val cache: Cache[K, V]
}

trait Cache[K, V] {
  def get(k: K): Option[V]
  def add(k: K, v: V): V
  def contents: List[K]
}

trait Cacheable {
  def length: Long
  def freshness: Long
  def stale = freshness < 0
}

abstract class MemoryCache[K, V <: Cacheable] extends MapCache[K, V] with LimitedCache[K, V] {
  val hint: Int
  lazy val map: java.util.Map[K, V] = new java.util.concurrent.ConcurrentHashMap[K, V](hint)
}

object MemoryCache {
  def apply[K, V <: Cacheable](hint: Int, maxsize: Long, shrinkby: Double): Cache[K, V] = {
    new Implementation[K, V](hint = hint, maxsize = maxsize, shrinkby = shrinkby)
  }
  private case class Implementation[K, V <: Cacheable](hint: Int, maxsize: Long, shrinkby: Double) extends MemoryCache[K, V]
}

protected trait RemoveableCache[K, V] extends Cache[K, V] {
  def remove(k: K): Option[V]
}

protected trait MapCache[K, V] extends RemoveableCache[K, V] {
  def get(k: K) = map.get(k) match { case v if null != v => Some(v) case null => None }
  def add(k: K, v: V): V = map.put(k, v)
  def remove(k: K) = map.remove(k) match { case v if null != v => Some(v) case null => None }
  import collection.JavaConversions._
  def contents = map.keySet.toList
  protected val map: java.util.Map[K, V]
}

protected trait LimitedCache[K, V <: Cacheable] extends RemoveableCache[K, V] with com.ibm.de.ebs.plm.scala.concurrent.ops.Exclusive {
  abstract override def get(k: K) = super.get(k) match { case Some(v) => if (v.stale) None else Some(v) case None => None }
  abstract override def add(k: K, v: V): V = {
    if (maxsize >= v.length) {
      if (maxsize < size + v.length) shrink(v.length); remove(k); super.add(k, v); inc(v.length)
    }
    v
  }
  abstract override def remove(k: K) = super.remove(k) match { case s @ Some(v) => inc(-v.length); s case None => None }
  private def shrink(need: Long): Unit = {
    def less(a: Option[V], b: Option[V]) = {
      (a, b) match { case (None, _) => true case (_, None) => false case (Some(a), Some(b)) => a.freshness < b.freshness }
    }
    exclusive {
      val target = scala.math.min(maxsize * (1 - shrinkby), maxsize - need).toLong
      contents.map(k => (k, get(k))).sortWith((a, b) => less(a._2, b._2)).foreach {
        case (k, v) => if (target < size || v.isEmpty) remove(k)
      }
    }
  }
  val maxsize: Long; require(0 < maxsize)
  val shrinkby: Double; require(0. <= shrinkby && shrinkby <= 1.)
  private var currentsize = new scala.concurrent.SyncVar[Long]; currentsize.set(0)
  private def size = currentsize.get
  private def inc(i: Long) = currentsize.set(size + i)
}
