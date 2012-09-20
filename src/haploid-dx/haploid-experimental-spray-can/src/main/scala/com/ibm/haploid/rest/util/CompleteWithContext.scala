package com.ibm.haploid.rest.util
import cc.spray.RequestContext

class CompleteWithContext(val ctx: RequestContext) extends ((RequestContext ⇒ Unit) ⇒ Unit) {

  def apply(f: RequestContext ⇒ Unit) = {
    f(ctx)
  }

}

trait CompleteWithContextTrait {
  def completeWithContext(ctx: RequestContext) = new CompleteWithContext(ctx)
}