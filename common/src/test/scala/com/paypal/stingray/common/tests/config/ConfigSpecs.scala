package com.paypal.stingray.common.tests.config

import com.paypal.stingray.common.config._
import org.specs2._
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import com.typesafe.config._

/**
 * Tests for [[com.paypal.stingray.common.config]]
 */
class ConfigSpecs extends Specification { override def is = s2"""

  RichConfig is an implicit optional wrapper on config getters.

  RichConfig:
    getOptionalString should
      return Some(value) if path exists           ${RConfig.string().ok}
      return None if path does not exist          ${RConfig.string().notFound}

    getOptionalInt should
      return Some(value) if path exists           ${RConfig.int().ok}
      return None if path does not exist          ${RConfig.int().notFound}
      throw if value cannot be converted          ${RConfig.int().failure}

    getOptionalLong should
      return Some(value) if path exists           ${RConfig.long().ok}
      return None if path does not exist          ${RConfig.long().notFound}
      throw if value cannot be converted          ${RConfig.long().failure}

    getOptionalBoolean should
      return Some(value) if path exists           ${RConfig.boolean().ok}
      return Some(true) with value "on"           ${RConfig.boolean().okOn}
      return Some(false) with value "off"         ${RConfig.boolean().okOff}
      return None if path does not exist          ${RConfig.boolean().notFound}
      throw if value cannot be converted          ${RConfig.boolean().failure}

    getOptionalList should
      return Some(value) if path exists           ${RConfig.simpleList().ok}
      return None if path does not exist          ${RConfig.simpleList().notFound}
      throw if value cannot be converted          ${RConfig.simpleList().failure}

    getOptionalDuration should
      return Some(value) if path exists           ${RConfig.duration().ok}
      return None if path does not exist          ${RConfig.duration().notFound}
      throw if value cannot be converted          ${RConfig.duration().failure}

"""

  trait Context extends CommonImmutableSpecificationContext {
    val config = ConfigFactory.load("test.conf")
  }


  object RConfig extends Context {
    case class string() {
      def ok = apply {
        config.getOptionalString("service.name") must beSome("matt")
      }
      def notFound = apply {
        config.getOptionalString("service.noname") must beNone
      }
    }

    case class int() {
      def ok = apply {
        config.getOptionalInt("service.num") must beSome(1)
      }
      def notFound = apply {
        config.getOptionalInt("service.nonum") must beNone
      }
      def failure = apply {
        config.getOptionalInt("service.name") must throwA[ConfigException.WrongType]

      }
    }

    case class long() {
      def ok = apply {
        config.getOptionalLong("service.num") must beSome(1L)
      }
      def notFound = apply {
        config.getOptionalLong("service.nonum") must beNone
      }
      def failure = apply {
        config.getOptionalLong("service.name") must throwA[ConfigException.WrongType]

      }
    }

    case class boolean() {
      def ok = apply {
        config.getOptionalBoolean("service.bool") must beSome(true)
      }
      def okOn = apply {
        config.getOptionalBoolean("service.bool-on") must beSome(true)
      }
      def okOff = apply {
        config.getOptionalBoolean("service.bool-off") must beSome(false)
      }
      def notFound = apply {
        config.getOptionalBoolean("service.nobool") must beNone
      }
      def failure = apply {
        config.getOptionalBoolean("service.name") must throwA[ConfigException.WrongType]
      }
    }

    case class simpleList() {
      def ok = apply {
        val aValue = config.getOptionalList("service.fruit")
        val list: List[String] = List("apples", "bananas", "oranges")
        aValue must beEqualTo(Some(list))
      }
      def notFound = apply {
        config.getOptionalList("service.veggies") must beNone
      }
      def failure = apply {
        config.getOptionalList("service.name") must throwA[ConfigException.WrongType]
      }
    }

    case class duration() {
      import scala.concurrent.duration._

      def ok = apply {
        config.getOptionalDuration("service.dur", SECONDS) must beSome(5)
      }
      def notFound = apply {
        config.getOptionalDuration("service.duration", SECONDS) must beNone
      }
      def failure = apply {
        config.getOptionalDuration("service.name", SECONDS) must throwA[ConfigException.BadValue]
      }
    }

  }



}
