package com.paypal.stingray.common.tests.option

import org.specs2.Specification
import org.specs2.execute.{Result => SpecsResult}
import com.paypal.stingray.common.option._
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for implicit [[com.paypal.stingray.common.option.RichOptionBoolean]]
 */
class RichOptionBooleanSpecs extends Specification { def is =
  "RichOptionBooleanSpecs".title                                                 ^
    """
  RichOptionBoolean adds helpful methods to Option[Boolean]
    """                                                                          ^
    "orFalse should"                                                             ^
      "return false given Some(false)"                                           ! OrFalse().someTrue ^
      "return true given Some(true)"                                             ! OrFalse().someFalse ^
      "return false given None"                                                  ! OrFalse().noneBoolean ^
                                                                                 end ^
    "orTrue should"                                                              ^
      "return false given Some(false)"                                           ! OrTrue().someTrue ^
      "return true given Some(true)"                                             ! OrTrue().someFalse ^
      "return true given None"                                                   ! OrTrue().noneBoolean ^
                                                                                 end

  case class OrFalse() extends CommonImmutableSpecificationContext {
    def someTrue: SpecsResult =  Some(true).orFalse must beTrue
    def someFalse: SpecsResult =  Some(false).orFalse must beFalse
    def noneBoolean: SpecsResult =  (None: Option[Boolean]).orFalse must beFalse
  }

  case class OrTrue() extends CommonImmutableSpecificationContext {
    def someTrue: SpecsResult =  Some(true).orTrue must beTrue
    def someFalse: SpecsResult =  Some(false).orTrue must beFalse
    def noneBoolean: SpecsResult =  (None: Option[Boolean]).orTrue must beTrue
  }
}
