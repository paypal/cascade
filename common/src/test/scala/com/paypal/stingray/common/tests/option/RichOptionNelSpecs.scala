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
 * Time: 3:09 PM
 */
class RichOptionNelSpecs  extends Specification { def is =
  "RichOptionNelSpecs".title                                                 ^
    """
  RichOptionNel adds a helpful method to Option[NoneEmptyList[A]]
    """                                                                          ^
    "list should"                                                             ^
      "return Nel.list given Some(Nel)"                                           ! OptionNel().someWorks ^
      "return List() given None"                                                  ! OptionNel().noneWorks ^
    end

  case class OptionNel() extends CommonImmutableSpecificationContext {
    def someWorks: SpecsResult =  Some(NonEmptyList(1, 2, 3)).list must beEqualTo(List(1, 2, 3))
    def noneWorks: SpecsResult =  (None: Option[NonEmptyList[Int]]).list must beEqualTo(List())
  }
}
