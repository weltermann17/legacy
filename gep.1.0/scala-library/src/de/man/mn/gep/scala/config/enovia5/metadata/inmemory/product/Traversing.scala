package de.man.mn.gep.scala.config.enovia5.metadata.inmemory.product

import de.man.mn.gep.scala.config.enovia5.metadata.inmemory._
import com.ibm.de.ebs.plm.scala.database.ConnectionFactory

trait Traversing[@specialized(Int) T] {

  protected val down: Boolean

  protected final lazy val up = !down

  protected val rootproduct: Boolean

  protected val groupby: Boolean

  protected val onlyonce: Boolean

  protected type Operator = (T, T) => T

  protected case class Input(assembly: Int, count: Int, depth: Int, f: Operator, initial: T)

  protected val canTraverse: Input => Boolean

  protected val onTraverse: Input => Any

  def traverse(root: Int, f: Operator, initial: T, authorizationidentifier: String): T = {
    nextlevel(root, 0).foldLeft(initial) {
      case (result, (assembly, count)) => f(result, traverse(assembly, count, 0, f, initial, authorizationidentifier))
    }
  }

  private def traverse(assembly: Int, count: Int, depth: Int, f: Operator, initial: T, authorizationidentifier: String): T = {
    if (relations.grantPermission(assembly)(authorizationidentifier)) {
      if (!(onlyonce && processed.contains(assembly))) {
        if (onlyonce) processed.add(assembly)
        push(assembly, depth)
        if (canTraverse(Input(assembly, count, depth, f, initial))) {
          onTraverse(Input(assembly, count, depth, f, initial))
          val current = if (down) relations.child(assembly) else relations.parent(assembly)
          if (down || !relations.productparent(assembly)) {
            nextlevel(current, depth + 1).foldLeft(initial) {
              case (result, (assembly, count)) => f(result, traverse(assembly, count, depth + 1, f, initial, authorizationidentifier))
            }
          } else {
            initial
          }
        } else {
          initial
        }
      } else {
        initial
      }
    } else {
      initial
    }
  }

  protected def path: String = {
    val s = new StringBuilder
    stack.foreach(s.append(_).append("-"))
    s.toString
  }

  private def nextlevel(current: Int, depth: Int): Set[(Int, Int)] = {
    val parents = if (rootproduct && down && 0 == depth) productparents else versionparents
    val next = if (groupby) {
      if (down) {
        relations.groupByRowId(relations.parent.lookup(current) & parents, relations.child)
      } else {
        relations.groupByRowId(relations.child.lookup(current), relations.parent)
      }
    } else {
      if (down) {
        (relations.parent.lookup(current) & parents).map((_, 1))
      } else {
        (relations.child.lookup(current) & parents).map((_, 1))
      }
    }
    // if (0 < next.size) println("nextlevel " + next)
    next
  }

  private def push(assembly: Int, depth: Int) = {
    while (stack.size > depth) stack.pop
    stack.push(assembly)
  }

  private lazy val stack = new collection.mutable.Stack[Int]
  private lazy val processed = new collection.mutable.BitSet

  protected lazy val relations = Repository(classOf[AssemblyRelations])
  private lazy val productparents = relations.productparent.lookup(true)
  private lazy val versionparents = relations.productparent.lookup(false)

}

class TraverseInstances(val rootproduct: Boolean)
  extends Traversing[Int] {
  val down = true
  val onlyonce = false
  val groupby = false
  val canTraverse = (_: Input) => true
  val onTraverse = (input: Input) => ()
  def traverse(root: Int, authorizationidentifier: String): Int = {
    traverse(root, (a: Int, b: Int) => a + b, 1, authorizationidentifier) - 1
  }
}

class TraverseVersions(val rootproduct: Boolean, val down: Boolean)
  extends Traversing[Int] {
  val result = new collection.mutable.BitSet
  val onlyonce = false
  val groupby = false
  val canTraverse = (_: Input) => true
  private lazy val threshold = Repository(classOf[Versions]).size
  val onTraverse = (input: Input) => {
    val index = input.assembly
    if (down) {
      if (0 < input.depth) {
        result.add(relations.child(index))
      }
    } else {
      val parent = relations.parent(index)
      if (relations.productparent(index)) result.add(parent + threshold) else result.add(parent)
    }
  }
  def traverse(root: Int, authorizationidentifier: String): Set[Int] = {
    traverse(root, (_: Int, _: Int) => 0, 0, authorizationidentifier)
    result.toImmutable
  }
}

