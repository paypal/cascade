package com.stackmob.tests.common.stats

import org.specs2.{ScalaCheck, Specification}
import org.specs2.mock.Mockito
import org.scalacheck._
import org.scalacheck.Prop._
import scala.concurrent.{ExecutionContext, Future}
import java.util.concurrent.Executors
import com.stackmob.tests.common.scalacheck.Generators

/**
 * Created by Andrew Harris
 * On 8/19/13 at 8:17 PM
 */
class DummyStatsDSpecs
  extends Specification
  with Mockito
  with ScalaCheck
  with Generators { override def is =
  "DummyStatsDSpecs".title                              ^
  "Tests the functionality of the DummyStatsD object"   ^
                                                        endp^
  "DummyStatsD should be able to"                       ^
    "Increment a key by one"                            ! IncrementOne().works ^
    "Increment a key by any positive int"               ! IncrementMany().works ^
    "Decrement a key by one"                            ! DecrementOne().works ^
    "Decrement a key by any positive int"               ! DecrementMany().works ^
    "Set a timing"                                      ! SetTiming().works ^
    "Overwrite a timing"                                ! OverwriteTiming().works ^
    "Clear counts and timings"                          ! ClearCountsAndTimings().works ^
    "Accept a timer for a method"                       ! AcceptTimer().works ^
    "Accept multiple timers for a method"               ! AcceptTimers().works ^
    "Accept a timer for a Future"                       ! AcceptFutureTimer().works ^
    "Accept multiple timers for a Future"               ! AcceptFutureTimers().works ^
                                                        end

  // only used for the method-accepting tests
  trait Context {
    def f: Unit = ()
    def fu: Future[Unit] = Future.successful(())
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  }

  case class IncrementOne() {
    def works = forAll(genNonEmptyAlphaStr) { (key) =>
      val d = new DummyStatsD()
      d.increment(key)
      d.getCalls(key) must beEqualTo(1)
    }
  }

  case class IncrementMany() {
    def works = forAll(genNonEmptyAlphaStr, Gen.posNum[Int]) { (key, i) =>
      val d = new DummyStatsD()
      d.increment(key, i)
      d.getCalls(key) must beEqualTo(i)
    }
  }

  case class DecrementOne() {
    def works = forAll(genNonEmptyAlphaStr) { (key) =>
      val d = new DummyStatsD()
      d.decrement(key)
      d.getCalls(key) must beEqualTo(-1)
    }
  }

  case class DecrementMany() {
    def works = forAll(genNonEmptyAlphaStr, Gen.posNum[Int]) { (key, i) =>
      val d = new DummyStatsD()
      d.decrement(key, i)
      d.getCalls(key) must beEqualTo(-(i))
    }
  }

  case class SetTiming() {
    def works = forAll(genNonEmptyAlphaStr, Gen.posNum[Int]) { (key, msec) =>
      val d = new DummyStatsD()
      d.timing(key, msec)
      d.getCalls(key) must beEqualTo(msec)
    }
  }

  case class OverwriteTiming() {
    def works = forAll(genNonEmptyAlphaStr, Gen.posNum[Int], Gen.posNum[Int]) { (key, msec, newMsec) =>
      val d = new DummyStatsD()
      d.timing(key, msec)
      d.timing(key, newMsec)
      d.getCalls(key) must beEqualTo(newMsec)
    }
  }

  case class ClearCountsAndTimings() {
    def works = forAll(genNonEmptyAlphaStr, genNonEmptyAlphaStr, Gen.posNum[Int]) { (key1, key2, msec) =>
      val d = new DummyStatsD()
      d.increment(key1)
      d.timing(key2, msec)
      d.resetCalls
      (d.getCalls(key1) must beEqualTo(0)) and (d.getCalls(key2) must beEqualTo(0))
    }
  }

  case class AcceptTimer() extends Context {
    def works = forAll(genNonEmptyAlphaStr) { (key) =>
      val d = new DummyStatsD()
      d.timeMethod(key)(f)
      d.getCalls(key) must beGreaterThan(0)
    }
  }

  case class AcceptTimers() extends Context {
    def works = forAll(genNonEmptyAlphaStr, genNonEmptyAlphaStr) { (key1, key2) =>
      val d = new DummyStatsD()
      d.timeMethods(key1, key2)(f)
      (d.getCalls(key1) must beGreaterThan(0)) and (d.getCalls(key2) must beGreaterThan(0))
    }
  }

  case class AcceptFutureTimer() extends Context {
    def works = forAll(genNonEmptyAlphaStr) { (key) =>
      val d = new DummyStatsD()
      d.timeFutureMethod(key)(fu)
      d.getCalls(key) must beGreaterThan(0)
    }
  }

  case class AcceptFutureTimers() extends Context {
    def works = forAll(genNonEmptyAlphaStr, genNonEmptyAlphaStr) { (key1, key2) =>
      val d = new DummyStatsD()
      d.timeFutureMethods(key1, key2)(fu)
      (d.getCalls(key1) must beGreaterThan(0)) and (d.getCalls(key2) must beGreaterThan(0))
    }
  }

}
