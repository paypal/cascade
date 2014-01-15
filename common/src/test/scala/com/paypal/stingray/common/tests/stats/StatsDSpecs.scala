package com.paypal.stingray.common.tests.stats

import org.specs2.{Specification, SpecificationLike, ScalaCheck}
import org.scalacheck._
import org.scalacheck.Prop._
import com.paypal.stingray.common.tests.scalacheck.Generators
import com.paypal.stingray.common.values.{StaticValues, StaticValuesFromServiceNameComponent}
import com.paypal.stingray.common.option._
import org.specs2.mock.Mockito
import com.paypal.stingray.common.stats.{StatsDCommon, StatsdClient}
import com.paypal.stingray.common.env.EnvironmentCommon
import scala.concurrent.{ExecutionContext, Future}

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 10/7/13
 * Time: 10:59 AM
 */
class StatsDSpecs extends Specification with Mockito with ScalaCheck with Generators with EnvironmentCommon { def is =
  "StatsDSpecs".title                                                                                                   ^
  """
    |StatsDSpecs tests features of the StatsD implementation.
  """.stripMargin                                                                                                       ^
  "The StatsD implementation should"                                                                                    ^
    "qualify the supplied key"                                                                                          ! timeMethod ^
    "not qualify the supplied key if someone has already qualified it"                                                  ! timeMethodQualified ^
    "qualify many supplied keys"                                                                                        ! timeMethods ^
    "not qualify a supplied key in a group if someone has already qualified it"                                         ! timeMethodsQualified ^
    "qualify the supplied key as a Future"                                                                              ! timeFutureMethod ^
    "qualify many supplied keys as a Future"                                                                            ! timeFutureMethods ^
    "qualify the supplied key and increment"                                                                            ! increment ^
    "qualify the supplied key and decrement"                                                                            ! decrement ^
    "qualify the supplied key and perform a timing"                                                                     ! timing ^
  end

  lazy val clustername = "mob1"

  lazy val prefix = s"$clustername.$getFullHostname."

  val svs = new StaticValues {
    override def get(v: String) = clustername.some
  }

  implicit lazy val executionContext = new ExecutionContext {
    def reportFailure(t: Throwable) { }

    def execute(runnable: Runnable) {
      runnable.run()
    }
  }

  class TestableStatsDCommon(client: StatsdClient) extends StatsDCommon(client, svs) {
    // need to return a fixed value since we can't mix matcher and real values
    override def timeSince(start: Long): Int = 0
  }

  def timeMethod = forAll(genNonEmptyAlphaStr) { key =>
    val client = mock[StatsdClient]
    client.timing(prefix + key, 0) returns true
    new TestableStatsDCommon(client).timeMethod(key)(())
    there was one(client).timing(prefix + key, 0)
  }

  def timeMethodQualified = forAll(genNonEmptyAlphaStr) { key =>
    val client = mock[StatsdClient]
    client.timing(prefix + key, 0) returns true
    new TestableStatsDCommon(client).timeMethod(prefix + key)(())
    there was one(client).timing(prefix + key, 0)
  }

  def timeMethods = forAll(Gen.listOf1(genNonEmptyAlphaStr).map(_.distinct)) { keys =>
    val client = mock[StatsdClient]
    keys.foreach { key =>
      client.timing(prefix + key, 0) returns true
    }
    new TestableStatsDCommon(client).timeMethods(keys: _*)(())
    keys must contain { key: String => there was one(client).timing(prefix + key, 0) }.forall
  }

  def timeMethodsQualified = forAll(Gen.listOf1(genNonEmptyAlphaStr).map(_.distinct)) { keys =>
    val client = mock[StatsdClient]
    keys.foreach { key =>
      client.timing(prefix + key, 0) returns true
    }
    new TestableStatsDCommon(client).timeMethods(keys.map(prefix + _): _*)(())
    keys must contain { key: String => there was one(client).timing(prefix + key, 0) }.forall
  }

  def timeFutureMethod = forAll(genNonEmptyAlphaStr) { key =>
    val client = mock[StatsdClient]
    client.timing(prefix + key, 0) returns true
    new TestableStatsDCommon(client).timeFutureMethod(key)(Future.successful(()))
    there was one(client).timing(prefix + key, 0)
  }

  def timeFutureMethods = forAll(Gen.listOf1(genNonEmptyAlphaStr).map(_.distinct)) { keys =>
    val client = mock[StatsdClient]
    keys.foreach { key =>
      client.timing(prefix + key, 0) returns true
    }
    new TestableStatsDCommon(client).timeFutureMethods(keys: _*)(Future.successful(()))
    keys must contain { key: String => there was one(client).timing(prefix + key, 0) }.forall
  }

  def increment = forAll(genNonEmptyAlphaStr) { key =>
    val client = mock[StatsdClient]
    client.increment(prefix + key, 0) returns true
    new TestableStatsDCommon(client).increment(key, 0)
    there was one(client).increment(prefix + key, 0)
  }

  def decrement = forAll(genNonEmptyAlphaStr) { key =>
    val client = mock[StatsdClient]
    client.decrement(prefix + key, 0) returns true
    new TestableStatsDCommon(client).decrement(key, 0)
    there was one(client).decrement(prefix + key, 0)
  }

  def timing = forAll(genNonEmptyAlphaStr) { key =>
    val client = mock[StatsdClient]
    client.timing(prefix + key, 0) returns true
    new TestableStatsDCommon(client).timing(key, 0)
    there was one(client).timing(prefix + key, 0)
  }
}
