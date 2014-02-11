package com.paypal.stingray.common.tests.values

import org.specs2._
import com.paypal.stingray.common.values.StaticValues
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests [[com.paypal.stingray.common.values.StaticValues]]
 */
class StaticValuesSpecs extends Specification with ScalaCheck { def is = s2"""

  StaticValues is an implementation of com.paypal.stingray.common.values.Values
  to read synchronously from local properties files.

  Constructors:
    Init with a root resource                                               ${Constructors.ByRootResource().ok}
    Init with serviceName should cause a lookup for serviceName.config      ${Constructors.ByServiceName().ok}
    Init with a URL should return configured StaticValue instance           ${Constructors.ByURL().ok}
    Init with no service name attempts to load from service.cluster.config  ${Constructors.NoParams().ok}
    Using defaultValues should return parameterless instance                ${Constructors.DefaultValues().ok}

  Get Methods:
    getOrDie should return value when property key exists             ${GetMethods.GetOrDie().successCase}
    getOrDie should throw when property key does not exist            ${GetMethods.GetOrDie().failureCase}
    getIntOrDie should return int value when property key exists      ${GetMethods.GetIntOrDie().successCase}
    getIntOrDie should throw when property key does not exist         ${GetMethods.GetIntOrDie().failureCase}
    getLongOrDie should return long value when property key exists    ${GetMethods.GetLongOrDie().successCase}
    getLongOrDie should throw when property key does not exist        ${GetMethods.GetLongOrDie().failureCase}

"""

  trait ConstructorsContext extends CommonImmutableSpecificationContext {
    // Nothing for now
  }

  trait GetContext extends CommonImmutableSpecificationContext {
    val url = getClass.getClassLoader.getResource("test.properties")
    val values = new StaticValues(url)
  }

  object Constructors {

    case class ByRootResource() extends ConstructorsContext {
      def ok = this {
        val values = new StaticValues("test")
        val property = values.get("some.property")
        property must beEqualTo(Some("somevalue"))
      }
    }

    case class ByServiceName() extends ConstructorsContext {
      override def before {
        System.setProperty("some-service.config", "src/test/resources/test.properties")
      }
      override def after {
        System.clearProperty("some-service.config")
      }
      def ok = this {

        val values = new StaticValues("some-service")
        val property = values.get("some.property")
        property must beEqualTo(Some("somevalue"))
      }
    }

    case class ByURL() extends ConstructorsContext {
      def ok = this {
        val url = getClass.getClassLoader.getResource("test.properties")
        val values = new StaticValues(url)
        val property = values.get("some.property")
        property must beEqualTo(Some("somevalue"))
      }
    }

    case class NoParams() extends ConstructorsContext {
      override def before {
        System.setProperty("stingray.cluster.config", "src/test/resources/test.properties")
      }
      override def after {
        System.clearProperty("stingray.cluster.config")
      }
      def ok = this {
        val values = new StaticValues()
        val property = values.get("some.property")
        property must beEqualTo(Some("somevalue"))
      }
    }

    case class DefaultValues() extends ConstructorsContext {
      override def before {
        System.setProperty("stingray.cluster.config", "src/test/resources/test.properties")
      }
      override def after {
        System.clearProperty("stingray.cluster.config")
      }
      def ok = this {
        val values = StaticValues.defaultValues
        val property = values.get("some.property")
        property must beEqualTo(Some("somevalue"))
      }
    }

  }

  object GetMethods {

    case class GetOrDie() extends GetContext {
      def successCase = this {
        val property = values.getOrDie("some.property")
        property must beEqualTo("somevalue")
      }
      def failureCase = this {
        values.getOrDie("some.other.property") must throwA[IllegalStateException]
      }
    }

    case class GetIntOrDie() extends GetContext {
      def successCase = this {
        val property = values.getIntOrDie("some.num")
        property must beEqualTo(1234)
      }
      def failureCase = this {
        values.getIntOrDie("some.not.int") must throwA[IllegalStateException]
      }
    }

    case class GetLongOrDie() extends GetContext {
      def successCase = this {
        val property = values.getLongOrDie("some.num")
        property must beEqualTo(1234L)
      }
      def failureCase = this {
        values.getLongOrDie("some.not.long") must throwA[IllegalStateException]
      }
    }

  }
}
