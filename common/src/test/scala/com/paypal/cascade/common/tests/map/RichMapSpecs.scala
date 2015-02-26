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
package com.paypal.cascade.common.tests.map

import org.specs2.Specification
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext
import com.paypal.cascade.common.map.RichMap

/**
 * Tests for implicit [[com.paypal.cascade.common.map.RichMap]]
 */
class RichMapSpecs extends Specification { override def is = s2"""

  RichMap is a wrapper for Map[A, B] for convenience methods.

  RichMap[A, B]#orNone should
    return Some for a map with items                          ${OrNone().nonEmpty}
    return None for an empty map                              ${OrNone().empty}

"""

  trait Context extends CommonImmutableSpecificationContext {
    protected lazy val m: Map[Int, Int] = Map(1 -> 2, 3 -> 4)
    protected lazy val e: Map[Int, Int] = Map.empty
  }

  case class OrNone() extends Context {

    def nonEmpty = {
      m.orNone must beSome
    }

    def empty = {
      e.orNone must beNone
    }

  }


}
