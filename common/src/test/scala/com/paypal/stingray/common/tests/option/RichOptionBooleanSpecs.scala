package com.paypal.stingray.common.tests.option

import org.specs2.Specification
import com.paypal.stingray.common.option._
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for implicit [[com.paypal.stingray.common.option.RichOptionBoolean]]
 */
class RichOptionBooleanSpecs extends Specification { override def is = s2"""

  RichOptionBoolean adds helpful methods to Option[Boolean]

    orFalse should
      return false given Some(false)                                          ${OrFalse().someTrue}
      return true given Some(true)                                            ${OrFalse().someFalse}
      return false given None                                                 ${OrFalse().noneBoolean}

    orTrue should
      return false given Some(false)                                          ${OrTrue().someTrue}
      return true given Some(true)                                            ${OrTrue().someFalse}
      return true given None                                                  ${OrTrue().noneBoolean}

  """

  case class OrFalse() extends CommonImmutableSpecificationContext {
    def someTrue = {
      Some(true).orFalse must beTrue
    }
    def someFalse = {
      Some(false).orFalse must beFalse
    }
    def noneBoolean = {
      (None: Option[Boolean]).orFalse must beFalse
    }
  }

  case class OrTrue() extends CommonImmutableSpecificationContext {
    def someTrue = {
      Some(true).orTrue must beTrue
    }
    def someFalse = {
      Some(false).orTrue must beFalse
    }
    def noneBoolean= {
      (None: Option[Boolean]).orTrue must beTrue
    }
  }
}
