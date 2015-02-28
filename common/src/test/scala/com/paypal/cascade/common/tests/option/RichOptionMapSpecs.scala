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
package com.paypal.cascade.common.tests.option

import org.scalacheck.Gen
import org.specs2.Specification
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.specs2.ScalaCheck
import com.paypal.cascade.common.option.RichOptionMap
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for implicit [[com.paypal.cascade.common.option.RichOption]]
 */
class RichOptionMapSpecs extends Specification with ScalaCheck { override def is = s2"""

  RichOptionMap is a wrapper for Option[Map[A, B]]

  RichOptionMap[A, B]#orEmpty should
    return Map.empty on None                                ${OrEmpty().empty}
    return a full Map on Some                               ${OrEmpty().nonEmpty}

"""
  trait Context extends CommonImmutableSpecificationContext {
    protected lazy val e: Option[Map[Int, Int]] = None
  }

  case class OrEmpty() extends Context {

    def empty = {
      e.orEmpty must beEqualTo(Map.empty)
    }

    def nonEmpty = {
      forAll(nonEmptyMap[String, String](Gen.zip(arbitrary[String], arbitrary[String]))) { m =>
        Option(m).orEmpty must beEqualTo(m)
      }
    }
  }

}
