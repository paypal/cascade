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

import org.specs2._
import org.specs2.execute.{Result => SpecsResult}
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary._
import com.paypal.cascade.common.option._
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext

import scala.util.Try

/**
 * Tests for implicit [[com.paypal.cascade.common.option.Any2Option]]
 */
class Any2OptionSpecs extends Specification with ScalaCheck { override def is = s2"""

  Any2OptionSpecs adds helpful Option methods to any object in scope

    some should
      return Some(val) given val: AnyVal                                  ${SomeTest().someAnyVal}
      return Some(val) given val: AnyRef                                  ${SomeTest().someAnyRef}
      return Some(null) given null string                                 ${SomeTest().someNull}

    opt should
      return Some(val) given val: AnyVal                                  ${OptTest().optAnyVal}
      return Some(val) given val: AnyRef                                  ${OptTest().optAnyRef}
      return None given null string                                       ${OptTest().optNull}

 """

  case class SomeTest() extends CommonImmutableSpecificationContext {
    def someAnyVal = forAll(arbitrary[AnyVal]) { value =>
      value.some must beSome.like {
        case l => l must beEqualTo(value)
      }
    }
    def someAnyRef = forAll(arbitrary[List[String]]) { ref =>
      ref.some must beSome.like {
        case l => l must beTheSameAs(ref)
      }
    }
    def someNull = {
      val s: String = null
      Try(s.some) must beFailedTry.withThrowable[IllegalArgumentException]
    }
  }

  case class OptTest() extends CommonImmutableSpecificationContext {
    def optAnyVal = forAll(arbAnyVal) { value =>
      value.opt must beSome.like {
        case l => l must beEqualTo(value)
      }
    }
    def optAnyRef = forAll(arbitrary[List[String]]) { ref =>
      ref.opt must beSome.like {
        case l => l must beTheSameAs(ref)
      }
    }
    def optNull = {
      val s: String = null
      s.opt must beNone
    }
  }

}
