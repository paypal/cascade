package com.stackmob.tests.common.option

import scalaz._
import Scalaz._
import org.specs2.Specification
import org.specs2.execute.{Result => SpecsResult}
import com.paypal.stingray.common.option._
import java.util.concurrent.atomic.AtomicInteger
import com.stackmob.tests.common.util.CommonImmutableSpecificationContext

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 4/30/13
 * Time: 2:26 PM
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