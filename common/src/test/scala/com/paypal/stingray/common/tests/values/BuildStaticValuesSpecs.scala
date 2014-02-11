package com.paypal.stingray.common.tests.values

import org.specs2._
import com.paypal.stingray.common.values.BuildStaticValues
import com.paypal.stingray.common.values.StaticValues
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests [[com.paypal.stingray.common.values.BuildStaticValues]]
 */
class BuildStaticValuesSpecs extends Specification with ScalaCheck { def is = s2"""

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
      override def before {
        System.setProperty("some-service.config", "src/test/resources/test.properties")
      }
      override def after {
        System.clearProperty("some-service.config")
      }
      def ok = this {
        val sv = new StaticValues("some-service")
        val bsv = new BuildStaticValues(sv)
        bsv must not beNull
      }
    }
  }

}
