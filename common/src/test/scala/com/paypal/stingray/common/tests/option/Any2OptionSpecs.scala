package com.paypal.stingray.common.tests.option

import org.specs2._
import org.specs2.execute.{Result => SpecsResult}
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary._
import com.paypal.stingray.common.option._
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for implicit [[com.paypal.stingray.common.option.Any2Option]]
 */
class Any2OptionSpecs extends Specification with ScalaCheck { def is = s2"""

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
      s.some must beSome.like {
        case o => o must beNull
      }
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
