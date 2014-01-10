package com.stackmob.tests.common.stats

import scalaz._
import Scalaz._
import org.specs2.{Specification, SpecificationLike, ScalaCheck}
import org.scalacheck._
import org.scalacheck.Prop._
import com.stackmob.tests.common.scalacheck.Generators
import com.paypal.stingray.common.values.{StaticValues, StaticValuesFromServiceNameComponent}
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
class StatsDSpecs extends Specification with Mockito with ScalaCheck with Generators with EnvironmentCommon { def is = s2"""

The StatsD implementation should
  timeMethod qualify the supplied key                                                $timeMethod
  timeMethod not qualify the supplied key if some moron has already qualified it     $timeMethodQualified
  timeMethods qualify the supplied key                                               $timeMethods
  timeMethods not qualify the supplied key if some moron has already qualified it    $timeMethodsQualified
  timeFutureMethod qualify the supplied key                                          $timeFutureMethod
  timeFutureMethods qualify the supplied key                                         $timeFutureMethods
  increment qualify the supplied key                                                 $increment
  decrement qualify the supplied key                                                 $decrement
  timing qualify the supplied key                                                    $timing
"""

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
    keys.map { key =>
      there was one(client).timing(prefix + key, 0)
    } reduce {_ and _}
  }

  def timeMethodsQualified = forAll(Gen.listOf1(genNonEmptyAlphaStr).map(_.distinct)) { keys =>
    val client = mock[StatsdClient]
    keys.foreach { key =>
      client.timing(prefix + key, 0) returns true
    }
    new TestableStatsDCommon(client).timeMethods(keys.map(prefix + _): _*)(())
    keys.map { key =>
      there was one(client).timing(prefix + key, 0)
    } reduce {_ and _}
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
    keys.map { key =>
      there was one(client).timing(prefix + key, 0)
    } reduce {_ and _}
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
