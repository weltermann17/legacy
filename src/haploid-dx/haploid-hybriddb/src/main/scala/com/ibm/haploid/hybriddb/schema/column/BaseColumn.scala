//package com.ibm.haploid
//
//package hybriddb
//
//package schema
//
//package column 
//
//import core.reflect.boxed
//import core.reflect.primitive
//
///**
// * This abstract base class is used internally in haploid-hybriddb.
// */
//abstract class BaseColumn[T](implicit m: Manifest[T])
//  extends Column[T] with Serializable {
//
//  /**
//   * throws an IndexOutOfBoundsException
//   */
//  def get(index: Int): T
//
//  def apply(index: Int) = get(index)
//
//  def apply(index: Int, value: T): Unit = try {
//    set(index, value)
//  } catch {
//    case _ => fallback(index, value)
//  }
//
//  def set(index: Int, any: Any): Unit = apply(index, any.asInstanceOf[T])
//
//  def set = setIf
//
//  protected[this] def complete
//
//  protected[this] var setIf: (Int, T) => Unit
//
//  final def filled = {
//    setIf = (_: Int, _: T) => throw new UnsupportedOperationException("After filling is complete no more calls to set() are allowed.")
//    complete
//  }
//
//  /**
//   * Yes, this is ugly, but it saves a lot of boilerplate code: we look for a constructor that takes either
//   * a primitive (Int) or a boxed value (java.lang.Integer); if this fails we look for a static method named
//   * 'valueOf' with either a primitve or a boxed parameter.
//   */
//  private[this] def fallback(index: Int, value: T): Unit = {
//    val fallbackvalue = if (m.erasure.isPrimitive) {
//      try {
//        val cons = m.erasure.getConstructor(value.getClass)
//        cons.newInstance(value.asInstanceOf[AnyRef])
//      } catch {
//        case _ =>
//          try {
//            val cons = m.erasure.getConstructor(boxed(value.getClass))
//            cons.newInstance(value.asInstanceOf[AnyRef])
//          } catch {
//            case _ =>
//              val method = m.erasure.getMethod("valueOf", value.getClass)
//              method.invoke(null, value.asInstanceOf[AnyRef])
//          }
//      }
//    } else {
//      try {
//        val cons = m.erasure.getConstructor(value.getClass)
//        cons.newInstance(value.asInstanceOf[AnyRef])
//      } catch {
//        case _ =>
//          try {
//            val cons = m.erasure.getConstructor(primitive(value.getClass))
//            cons.newInstance(value.asInstanceOf[AnyRef])
//          } catch {
//            case _ =>
//              val method = m.erasure.getMethod("valueOf", value.getClass)
//              method.invoke(null, value.asInstanceOf[AnyRef])
//          }
//      }
//    }
//    set(index, fallbackvalue.asInstanceOf[T])
//  }
//
//}
//
