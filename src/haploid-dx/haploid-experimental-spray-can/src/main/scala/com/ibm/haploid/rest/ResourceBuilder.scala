package com.ibm.haploid.rest
import cc.spray.RequestContext
import org.scalastuff.scalabeans.Preamble._

private[rest] trait ResourceBuilder {

  sealed abstract class ResourceBuilder(val className: String, val properties: List[String], val size: Int) {
    require(properties.size == size)

    val clazz = Class.forName(className)
    val desc = descriptorOf(clazz);

    protected def buildInstance(): HaploidResource = {
      clazz.newInstance().asInstanceOf[HaploidResource]
    }

    protected def buildInstanceWithProperties(values: List[Any]): HaploidResource = {
      val resource = buildInstance()
      setProperties(resource, values)
    }

    def completeWith(s: Any*): RequestContext ⇒ Unit = {
      buildInstanceWithProperties(s.toList).executeRequest()
    }

    private def setProperties(inst: HaploidResource, values: List[Any]): HaploidResource = {
      require(values.size == properties.size)

      for (i ← 0 to properties.size - 1) {
        desc.set(inst, properties(i), values(i))
      }

      inst
    }

  }

  class ResourceBuilder0(className: String) extends ResourceBuilder(className, List[String](), 0) {
    def completeWith: RequestContext ⇒ Unit = super.completeWith()
  }

  class ResourceBuilder1[T](className: String, properties: List[String])
    extends ResourceBuilder(className, properties, 1) {
    def completeWith(s1: T): RequestContext ⇒ Unit = super.completeWith(s1)
  }

  class ResourceBuilder2[A, B](className: String, properties: List[String])
    extends ResourceBuilder(className, properties, 2) {
    def completeWith(s1: A, s2: B): RequestContext ⇒ Unit = super.completeWith(s1, s2)
  }

  class ResourceBuilder3[A, B, C](className: String, properties: List[String])
    extends ResourceBuilder(className, properties, 3) {
    def completeWith(s1: A, s2: B, s3: C): RequestContext ⇒ Unit = super.completeWith(s1, s2, s3)
  }

  class ResourceBuilder4[A, B, C, D](className: String, properties: List[String])
    extends ResourceBuilder(className, properties, 4) {
    def completeWith(s1: A, s2: B, s3: C, s4: D): RequestContext ⇒ Unit = super.completeWith(s1, s2, s3, s4)
  }

  class ResourceBuilder5[A, B, C, D, E](className: String, properties: List[String])
    extends ResourceBuilder(className, properties, 5) {
    def completeWith(s1: A, s2: B, s3: C, s4: D, s5: E): RequestContext ⇒ Unit = super.completeWith(s1, s2, s3, s4, s5)
  }

}