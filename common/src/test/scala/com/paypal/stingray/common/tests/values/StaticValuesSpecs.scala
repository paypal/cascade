package com.paypal.stingray.common.tests.values

import org.specs2._
import com.paypal.stingray.common.values.StaticValues
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import com.paypal.stingray.common.enumeration._

/**
 * Tests [[com.paypal.stingray.common.values.StaticValues]]
 */
class StaticValuesSpecs extends Specification with ScalaCheck { def is = s2"""

  StaticValues is an implementation of com.paypal.stingray.common.values.Values
  to read synchronously from local properties files.

  Get Methods:
    getOrDie should return value when property key exists             ${GetMethods.GetOrDie().successCase}
    getOrDie should throw when property key does not exist            ${GetMethods.GetOrDie().failureCase}

    getIntOrDie should return int value when property key exists      ${GetMethods.GetIntOrDie().successCase}
    getIntOrDie should throw when value is incorrect type             ${GetMethods.GetIntOrDie().failureCase}

    getLongOrDie should return long value when property key exists    ${GetMethods.GetLongOrDie().successCase}
    getLongOrDie should throw when value is incorrect type            ${GetMethods.GetLongOrDie().failureCase}

    getBool should return boolean value when property key exists      ${GetMethods.GetBool().successCase}
    getBool should return None when value is incorrect type           ${GetMethods.GetBool().failureCase}

    getEnum should return enum value when property key exists         ${GetMethods.GetEnum().successCase}
    getEnum should return None when value is incorrect type           ${GetMethods.GetEnum().failureCase}

    getSimpleList should return value when property key exists        ${GetMethods.GetSimpleList().successCase}

"""

  trait ConstructorsContext extends CommonImmutableSpecificationContext {
    // Nothing for now
  }

  trait GetContext extends CommonImmutableSpecificationContext {
    val url = getClass.getClassLoader.getResource("test.properties")
    val values = new StaticValues(url)
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

    case class GetBool() extends GetContext {
      def successCase = this {
        val property = values.getBool("some.bool")
        property must beEqualTo(Some(true))
      }
      def failureCase = this {
        val property = values.getBool("some.not.bool")
        property must beEqualTo(None)
      }
    }

    case class GetEnum() extends GetContext {
      sealed abstract class EnvironmentType extends Enumeration
      object EnvironmentType extends EnumUnapply[EnvironmentType] {

        object DEV extends EnvironmentType {
          val stringVal = "dev"
        }
        object PROD extends EnvironmentType {
          val stringVal = "prod"
        }

        implicit val environmentTypeRead: EnumReader[EnvironmentType] = new EnumReader[EnvironmentType] {
          override def read(s: String): Option[EnvironmentType] = s.toLowerCase match {
            case DEV.stringVal => Some(DEV)
            case PROD.stringVal => Some(PROD)
            case _ => None
          }
        }

      }
      def successCase = this {
        val property = values.getEnum[EnvironmentType]("some.enum")
        property must beEqualTo(Some(EnvironmentType.DEV))
      }
      def failureCase = this {
        val property = values.getEnum[EnvironmentType]("some.bool")
        property must beEqualTo(None)
      }
    }

    case class GetSimpleList() extends GetContext {

      def successCase = this {
        val property = values.getSimpleList("some.list")
        property must beEqualTo(Some(List("apples", "bananas", "oranges")))
      }
    }
  }
}
