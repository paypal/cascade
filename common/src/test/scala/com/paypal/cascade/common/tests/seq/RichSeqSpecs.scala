/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.common.tests.seq

import org.specs2.Specification
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext
import com.paypal.cascade.common.seq.RichSeq

/**
 * Tests for implicit [[com.paypal.cascade.common.seq.RichSeq]]
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
