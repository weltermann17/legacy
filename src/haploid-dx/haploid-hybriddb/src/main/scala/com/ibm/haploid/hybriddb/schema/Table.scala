package com.ibm.haploid

package hybriddb

package schema

import collection.IndexedSeqOptimized
import collection.mutable.{ Builder, ListBuffer }

import core.reflect.properties

/**
 * The essence of the hybrid database schema.
 */
class Table[C <: Any] private (val columns: C, s: Seq[Map[String, _]])(implicit manifest: Manifest[C])

  extends IndexedSeq[Map[String, _]]
  with IndexedSeqOptimized[Map[String, _], Table[C]]
  with Serializable {

  import Table._

  val length = s.length

  def apply(index: Int) = get(index)

  def get(index: Int): Map[String, _] = null // columns.mapValues(_(index))

  override def newBuilder = new Builder[Map[String, _], Table[C]] {

    def +=(elem: Map[String, _]) = { newseq += elem; this }

    def clear = newseq.clear

    def result = {
      val newcol = manifest.erasure.getConstructor(classOf[Int]).newInstance(newseq.size.asInstanceOf[AnyRef])
      fromSeq(newcol.asInstanceOf[C])(newseq)
    }

    private[this] val newseq = new ListBuffer[Map[String, _]]

  }

}

object Table {

  def fromSeq[C <: Any](col: C)(seq: Seq[Map[String, _]])(implicit manifest: Manifest[C]): Table[C] = {
    val table = new Table[C](col, seq)
//    var i = 0; seq.foreach { row => table.columns.foreach { case (name, column) => column.set(i, row(name)) }; i += 1 }
//    table.columns.filled
    table
  }

}

trait TableBuilder[C <: Any] {

  def apply(length: Int): C

  def apply(seq: Seq[Map[String, _]])(implicit manifest: Manifest[C]): Table[C] = Table.fromSeq(apply(seq.length))(seq)

}

