package com.paypal.stingray.common.tests.option

import org.specs2.Specification
import org.specs2.execute.{Result => SpecsResult}
import com.paypal.stingray.common.option._
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for implicit [[com.paypal.stingray.common.option.Any2Option]]
 */
class Any2OptionSpecs extends Specification { def is = s2"""

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
    def someAnyVal = {
      0L.some must beSome.like {
        case l => l must beEqualTo(0L)
      }
    }
    def someAnyRef = {
      List[String]().some must beSome.like {
        case l => l must beTheSameAs(List[String]())
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
    def optAnyVal = {
      0L.opt must beSome.like {
        case l => l must beEqualTo(0L)
      }
    }
    def optAnyRef ={
      List[String]().opt must beSome.like {
        case l => l must beTheSameAs(List[String]())
      }
    }
    def optNull = {
      val s: String = null
      s.opt must beNone
    }
  }
}
