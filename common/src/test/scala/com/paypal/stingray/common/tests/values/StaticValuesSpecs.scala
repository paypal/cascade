package com.paypal.stingray.common.tests.values

import org.specs2._
import org.scalacheck._
import scala.util.Try
import java.net.URL
import java.util.Properties
import com.paypal.stingray.common.values.StaticValues
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests [[com.paypal.stingray.common.values.StaticValues]]
 */
class StaticValuesSpecs extends Specification with ScalaCheck { def is = s2"""

  StaticValues is an implementation of com.paypal.stingray.common.values.Values
  to read synchronously from local properties files.

  Constructors:
    Init with a root resource                                           ${Constructors.ByRootResource().ok}
    Init with serviceName should cause a lookup for serviceName.config
    Init with a URL should return configured StaticValue instance


"""

  object Constructors {
    trait Context extends CommonImmutableSpecificationContext {
      override def before {
        System.setProperty("some-service", "somewhere")
      }

    }

    case class ByRootResource() {
      def ok = {
        val values = new StaticValues("test")
        val property = values.get("some.property")
        property must beEqualTo(Some("somevalue"))
      }
    }
  }
}
