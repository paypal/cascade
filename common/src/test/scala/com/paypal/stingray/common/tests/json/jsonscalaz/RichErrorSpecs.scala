package com.stackmob.tests.common.json.jsonscalaz

import org.specs2.Specification
import org.specs2.execute.{Result => SpecsResult}
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import com.stackmob.tests.common.util.CommonImmutableSpecificationContext

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.tests.json.jsonscalaz
 *
 * User: aaron
 * Date: 6/5/12
 * Time: 10:11 AM
 */

class RichErrorSpecs extends Specification { def is =
  "RichErrorSpecs".title                                                                                                ^
  """
  RichError is StackMob's pimp to JsonScalaz's Error class
  """                                                                                                                   ^
  "RichError#fold should"                                                                                               ^
    "return the right result based on the type of error it is"                                                          ! Fold().returnsCorrectResult ^
                                                                                                                        end
  trait Context extends CommonImmutableSpecificationContext

  case class Fold() extends Context {
    import com.paypal.stingray.common.json.jsonscalaz._
    def returnsCorrectResult: SpecsResult = {
      val err1 = UnexpectedJSONError(JNothing, JNothing.getClass)
      val res1 = err1.getClass.getCanonicalName
      val err2 = NoSuchFieldError("test", JNothing)
      val res2 = err2.getClass.getCanonicalName
      val err3 = UncategorizedError("test", "test", List())
      val res3 = err3.getClass.getCanonicalName
      (err1.fold(a => res1, b => res2, c => res3) must beEqualTo(res1)) and
      (err2.fold(a => res1, b => res2, c => res3) must beEqualTo(res2)) and
      (err3.fold(a => res1, b => res2, c => res3) must beEqualTo(res3))

    }
  }

}
