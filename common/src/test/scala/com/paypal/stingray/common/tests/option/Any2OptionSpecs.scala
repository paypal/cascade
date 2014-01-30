package com.paypal.stingray.common.tests.option

import org.specs2.Specification
import org.specs2.execute.{Result => SpecsResult}
import com.paypal.stingray.common.option._
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for implicit [[com.paypal.stingray.common.option.Any2Option]]
 */
class Any2OptionSpecs extends Specification { def is =
  "Any2OptionSpecs".title                                                        ^
    """
  Any2OptionSpecs adds helpful Option methods to any object in scope
    """                                                                          ^
    "orFalse should"                                                             ^
      "return false given Some(false)"                                           ! SomeTest().someAnyVal ^
      "return true given Some(true)"                                             ! SomeTest().someAnyRef ^
      "return false given None"                                                  ! SomeTest().someNull ^
                                                                                 end ^
    "orTrue should"                                                              ^
      "return false given Some(false)"                                           ! OptTest().optAnyVal ^
      "return true given Some(true)"                                             ! OptTest().optAnyRef ^
      "return true given None"                                                   ! OptTest().optNull ^
                                                                                 end

  case class SomeTest() extends CommonImmutableSpecificationContext {
    def someAnyVal: SpecsResult =  0L.some must beSome.like {
      case l => l must beEqualTo(0L)
    }
    def someAnyRef: SpecsResult = List[String]().some must beSome.like {
      case l => l must beTheSameAs(List[String]())
    }
    def someNull: SpecsResult = {
      val s: String = null
      s.some must beSome.like {
        case o => o must beNull
      }
    }
  }

  case class OptTest() extends CommonImmutableSpecificationContext {
    def optAnyVal: SpecsResult =  0L.opt must beSome.like {
      case l => l must beEqualTo(0L)
    }
    def optAnyRef: SpecsResult = List[String]().opt must beSome.like {
      case l => l must beTheSameAs(List[String]())
    }
    def optNull: SpecsResult = {
      val s: String = null
      s.opt must beNone
    }
  }
}
