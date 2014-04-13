package com.paypal.stingray.common.tests.seq

import org.specs2.Specification
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import com.paypal.stingray.common.seq.RichSeq

/**
 * Tests for implicit [[com.paypal.stingray.common.seq.RichSeq]]
 */

class RichSeqSpecs extends Specification { override def is = s2"""

  RichSeq is a wrapper for Seq[T]. It contains stuff that is good.

  RichSeq[T]#get should
    return Some(t) if the index was in bounds                                ${Get().inBoundsReturnsSome}
    return None if the index was out of bounds                               ${Get().outOfBoundsReturnsNone}

  """

  trait Context extends CommonImmutableSpecificationContext {
    protected lazy val s = Seq(1, 2, 3)
    protected lazy val wrapper = new RichSeq(s)
  }

  case class Get() extends Context {
    def inBoundsReturnsSome = {
      s.get(0) must beEqualTo(Some(s(0)))
    }
    def outOfBoundsReturnsNone = {
      (wrapper.get(-1) must beNone) and
      (wrapper.get(s.length) must beNone)
    }
  }

}
