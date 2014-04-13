package com.paypal.stingray.http.tests.resource

import org.specs2.Specification
import com.paypal.stingray.http.resource._
import spray.http.StatusCodes.BadRequest
import scala.concurrent.{Await, Future}
import spray.http.HttpResponse
import scala.util.Try
import scala.concurrent.duration.Duration

/**
 * Tests resource.scala in [[com.paypal.stingray.http.resource]]
 */
class ResourceSpecs extends Specification { override def is = s2"""

  Tests that exercise implicit classes of resource.scala

  RichOptionTryHalt#orErrorT should
    return the value if Some                                    ${ROptionTryHalt.orErrorT().some}
    throw a HaltException if None                               ${ROptionTryHalt.orErrorT().none}

  RichOptionFutureHalt#orError should
    wrap value in successful future if Some                     ${ROptionFutureHalt.orError().some}
    return a failed future if None                              ${ROptionFutureHalt.orError().none}

  RichEitherThrowableHalt#orThrowHaltExceptionWithErrorMessage should
    return the value if right                                   ${REitherThrowableHalt.orThrowHaltExceptionWithErrorMessage().ok}
    throw a Halt Exception if left                              ${REitherThrowableHalt.orThrowHaltExceptionWithErrorMessage().failure}

  RichEitherThrowableHalt#orErrorWithMessage should
    wrap value in successful future if right                    ${REitherThrowableHalt.orErrorWithMessage().ok}
    return a failed future with halt exception if left          ${REitherThrowableHalt.orErrorWithMessage().failure}

  RichTryHalt#orThrowHaltExceptionWithErrorMessage should
    return the value if success                                 ${RTryHalt.orThrowHaltExceptionWithErrorMessage().ok}
    throw a Halt Exception on failure                           ${RTryHalt.orThrowHaltExceptionWithErrorMessage().failure}

  RichTryHalt#orErrorWithMessage should
    wrap value in successful future if success                  ${RTryHalt.orErrorWithMessage().ok}
    return a failed future with halt exception on failure       ${RTryHalt.orErrorWithMessage().failure}

  RichEitherHalt#orError should
    wrap value in successful future if right                    ${REitherHalt.orError().ok}
    return a failed future with halt exception if left          ${REitherHalt.orError().failure}

  RichEitherHalt#orErrorNow should
    return value if right                                       ${REitherHalt.orErrorNow().ok}
    throw halt exception if left                                ${REitherHalt.orErrorNow().failure}

  RichBooleanTryHalt#orErrorT should
    return value if true                                        ${RBooleanHalt.orErrorT().ok}
    return halted future if false                               ${RBooleanHalt.orErrorT().failure}

  RichBooleanFutureHalt#orError should
    wrap value in successful future if true                     ${RBooleanHalt.orError().ok}
    return halted future if false                               ${RBooleanHalt.orError().failure}

  RichFuture#orHalt should
    wrap value in successful future if valid                    ${RFuture.orHalt().ok}
    return halted future if throws                              ${RFuture.orHalt().failure}

  RichIdentity#continue should
    wrap the function in a successful future                    ${RIdentity.continue().ok}

  RichThrowableHalt#haltWith should
    wrap the throwable in a future halt exception               ${RThrowableHalt.haltWith().ok}


  """

  object ROptionTryHalt {
    case class orErrorT() {
      def some = {
        val someOption = Option(3)
        val success = someOption.orErrorT()
        success must beSuccessfulTry[Int].withValue(3)
      }
      def none = {
        val noneOption = Option.empty[Int]
        val failure = noneOption.orErrorT()
        failure must beFailedTry[Int].withThrowable[HaltException]
      }
    }
  }

  object ROptionFutureHalt {
    case class orError() {
      def some = {
        val someOption = Option(3)
        val success = someOption.orError()
        success.value.get must beSuccessfulTry[Int].withValue(3)
      }
      def none = {
        val noneOption = Option.empty[Int]
        val failure = noneOption.orError()
        failure.value.get must beFailedTry[Int].withThrowable[HaltException]
      }
    }
  }

  object REitherThrowableHalt {
    case class orThrowHaltExceptionWithErrorMessage() {
       def ok = {
        Right("hi").orThrowHaltExceptionWithErrorMessage() must beEqualTo("hi")
       }
      def failure = {
        Left[Throwable, Unit](new Throwable("fail")).orThrowHaltExceptionWithErrorMessage() must throwA[HaltException]
      }
    }
    case class orErrorWithMessage() {
      def ok = {
        val successfulFuture = Right("hi").orErrorWithMessage()
        successfulFuture.value.get must beSuccessfulTry[String].withValue("hi")
      }
      def failure = {
        val failedFuture = Left[Throwable, Unit](new Throwable("fail")).orErrorWithMessage()
        failedFuture.value.get must beFailedTry[Unit].withThrowable[HaltException]
      }
    }
  }

  object RTryHalt {
    case class orThrowHaltExceptionWithErrorMessage() {
      def ok = {
        Try { "hi" }.orThrowHaltExceptionWithErrorMessage() must beEqualTo("hi")
      }
      def failure = {
        Try[Unit] { throw new Throwable("fail") }.orThrowHaltExceptionWithErrorMessage() must throwA[HaltException]
      }
    }
    case class orErrorWithMessage() {
      def ok = {
        val successfulFuture = Try { "hi" }.orErrorWithMessage()
        successfulFuture.value.get must beSuccessfulTry[String].withValue("hi")
      }
      def failure = {
        val failedFuture = Try[Unit] { throw new Throwable("fail") }.orErrorWithMessage()
        failedFuture.value.get must beFailedTry[Unit].withThrowable[HaltException]
      }
    }
  }

  object REitherHalt {
    case class orError() {
      def ok = {
        val successfulFuture = Right("hi").orError()
        successfulFuture.value.get must beASuccessfulTry[String].withValue("hi")
      }
      def failure = {
        val failedFuture = Left[Throwable, Unit](new Throwable("no")).orError()
        failedFuture.value.get must beAFailedTry[Unit].withThrowable[HaltException]
      }
    }
    case class orErrorNow() {
      def ok = {
        Right("hi").orErrorNow() must beEqualTo("hi")
      }
      def failure = {
        Left[Throwable, Unit](new Throwable("no")).orErrorNow() must throwA[HaltException]
      }
    }
  }

  object RBooleanHalt {
    case class orErrorT() {
      def ok = {
        true.orErrorT() must beASuccessfulTry[Unit]
      }
      def failure = {
        false.orErrorT() must beAFailedTry[Unit].withThrowable[HaltException]
      }
    }
    case class orError() {
      def ok = {
        val successfulFuture = true.orError()
        successfulFuture.value.get must beASuccessfulTry[Unit]
      }
      def failure = {
        val failedFuture = false.orError()
        failedFuture.value.get must beAFailedTry[Unit].withThrowable[HaltException]
      }
    }
  }

  object RFuture {
    case class orHalt() {
      def ok = {
        val successfulFuture = Future("hi").orHalt { case e: Throwable => HttpResponse(BadRequest) }
        Try { Await.result(successfulFuture, Duration.Inf) } must beSuccessfulTry[String].withValue("hi")
      }
      def failure = {
        val haltedFuture = Future[Unit] { throw new Throwable("fail")}.orHalt { case e: Throwable => HttpResponse(BadRequest) }
        Try { Await.result(haltedFuture, Duration.Inf) } must beFailedTry[Unit].withThrowable[HaltException]
      }
    }
  }

  object RIdentity {
    case class continue() {
      def ok = {
        val successfulFuture = "hi".continue
        successfulFuture.value.get must beSuccessfulTry[String].withValue("hi")
      }
    }
  }

  object RThrowableHalt {
    case class haltWith() {
      def ok = {
        val failedFuture = new Throwable("no").haltWith(BadRequest)()
        failedFuture.value.get must beFailedTry[Unit].withThrowable[HaltException]
      }
    }
  }

}
