package com.paypal.stingray.common.tests.values

import org.specs2._
import org.specs2.mock.Mockito
import com.paypal.stingray.common.values.BuildStaticValues
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import java.util.Date

/**
 * Tests [[com.paypal.stingray.common.values.BuildStaticValues]]
 */
class BuildStaticValuesSpecs extends Specification with Mockito { def is = s2"""

  BuildStaticValues inherits from com.paypal.stingray.common.values.StaticValues
  and adds methods to get the current date as well as a dependency list of a given
  service.

  Constructors:
    Default constructor will initialize with class build.properties file      ${Constructors.Default().ok}

"""
  trait ConstructorsContext extends CommonImmutableSpecificationContext {
    // Nothing for now
  }

  object Constructors {

    case class Default() extends ConstructorsContext {
      def ok = this {
        val bsv = spy(new BuildStaticValues)
        bsv.get("some.date") returns Some("140226091500PST")
        val value = bsv.getDate("some.date")
        value must beSome[Date]
      }
    }
  }

}
