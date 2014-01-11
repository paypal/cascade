package com.paypal.stingray.http.tests.client

import org.specs2.Specification
import org.specs2.matcher.MatchResult
import scalaz._
import Scalaz._
import com.paypal.stingray.http.client._
import com.stackmob.newman._
import response._
import dsl._
import com.stackmob.newman.test.DummyHttpClient
import com.stackmob.newman.{Headers, RawBody}
import language.reflectiveCalls
import com.paypal.stingray.common.tests.stats.DummyStatsD
import com.paypal.stingray.common.stats.StatsD
import com.paypal.stingray.common.validation.ThrowableValidation
import scala.concurrent.Future

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.http.tests.client
 *
 * User: aaron
 * Date: 2/13/13
 * Time: 12:23 PM
 */
class BaseClientSpecs extends Specification { def is =
  "BaseClientSpecs".title                                                                                               ^
  """BaseClient is the root for all clients to services, internal and external"""                                       ^
  "successful call returns success"                                                                                     ! SuccessCall().succeeds ^ end ^
  "failure call returns failure"                                                                                        ! FailureCall().fails ^ end ^
  "2 calls to the same client does not produce an NPE"                                                                  ! TwoCalls().doesntThrow ^ end

  private sealed trait Context {
    implicit val statsD: StatsD = new DummyStatsD()

    protected def getHttpClient(response: HttpResponse) = {
      new DummyHttpClient(Future.successful(response))
    }
    protected val successResponse = HttpResponse(HttpResponseCode.Ok, Headers.empty, RawBody.empty)
    protected val successInt = 22
    protected val failureResponse = HttpResponse(HttpResponseCode.NotFound, Headers.empty, RawBody.empty)
    protected val failureThrowable: Throwable = new Exception("test failure")
    protected val client = new BaseClient("testService", http, "stackmob.com", 1234, getHttpClient(successResponse)) {
      def succ: ThrowableValidation[Int] = instrument[Int](successInt.success[Throwable])
      def fails: ThrowableValidation[Int] = instrument[Int](failureThrowable.fail[Int])
    }
  }

  private case class SuccessCall() extends Context {
    def succeeds = client.succ.toEither must beRight.like {
      case r => r must beEqualTo(successInt)
    }
  }

  private case class FailureCall() extends Context {
    def fails = client.fails.toEither must beLeft.like {
      case t => t must beEqualTo(failureThrowable)
    }
  }

  private case class TwoCalls() extends Context {
    def doesntThrow = {
      val call1 = client.succ
      val call2 = client.succ
      val partialFunc: PartialFunction[Int, MatchResult[_]] = { case i: Int => i must beEqualTo(successInt) }
      (call1.toEither must beRight.like(partialFunc)) and
      (call2.toEither must beRight.like(partialFunc))
    }
  }
}
