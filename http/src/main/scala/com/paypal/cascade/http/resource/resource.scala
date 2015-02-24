/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.http

import spray.http._
import spray.http.HttpEntity._
import spray.http.StatusCodes._
import com.paypal.cascade.common.option._
import com.paypal.cascade.common.trys._
import scala.concurrent._
import scala.concurrent.Future
import scala.util.{Success, Failure, Try}

/**
 * Utility methods for turning everyday datatypes into Trys and Futures that can possibly return a
 * [[com.paypal.cascade.http.resource.HaltException]]. Methods of the form `orHalt` create a Future.
 * Methods of the form `orHaltT` return a Try.
 */

package object resource {

  /** For resources that do not expect a body in requests */
  type NoBody = Option[String]

  /** For resources that do not perform any degree of authorization of incoming requests */
  type NoAuth = Unit

  /**
   * Stop further processing, and yield an error response
   * @param status the response code to return
   * @param entity the response body to return, if any
   * @param headers headers to return in the response, if any
   * @tparam T the expected return type, if processing were to continue
   * @return a failed Future, containing an error response
   */
  def halt[T](status: StatusCode,
              entity: HttpEntity = Empty,
              headers: List[HttpHeader] = Nil): Future[T] = none[T].orHaltWith(status, entity, headers)

  /**
   * Implicit wrapper to allow optional values to halt or throw
   *
   * {{{
   *   import com.paypal.cascade.http.resource._
   *   Option("hi").orError()  // Future("hi")
   * }}}
   *
   * @param v the option to wrap
   * @tparam A the type of the wrapped option
   */
  implicit class RichOptionTryHalt[A](v: Option[A]) {

    /**
     * Return the value inside this option, or fail
     * @param halt the HttpResponse to use for the failure
     * @return the value inside this option, or a failed Future
     */
    def orHaltT(halt: => HttpResponse): Try[A] = v match {
      case Some(a) => Success(a)
      case None => Failure(new HaltException(halt))
    }

    /**
     * Return the value inside this option, or fail
     * @param status the response code to return on failure
     * @param entity the response body to return on failure, if any
     * @param headers headers to return on failure, if any
     * @return the value inside this option, or a failed Future
     */
    def orHaltWithT(status: => StatusCode,
                    entity: => HttpEntity = Empty,
                    headers: => List[HttpHeader] = Nil): Try[A] = {
      orHaltT(HttpResponse(status, entity, headers))
    }

    /**
     * Return the value inside this option, or fail with a 500 Internal Server Error
     * @param entity the response body to return on failure, if any
     * @param headers headers to return on failure, if any
     * @return the value inside this option, or a failed Future with a 500 error
     */
    def orErrorT(entity: => HttpEntity = Empty,
                 headers: => List[HttpHeader] = Nil): Try[A] = {
      orHaltWithT(InternalServerError, entity, headers)
    }

  }

  /**
   * Implicit wrapper to allow optional values to halt or throw
   *
   * {{{
   *   import com.paypal.cascade.http.resource._
   *   Option("hi").orError()  // Future("hi")
   * }}}
   *
   * @param v the option to wrap
   * @tparam A the type of the wrapped option
   */
  implicit class RichOptionFutureHalt[A](v: Option[A]) {

    /**
     * Return the value inside this option, or fail
     * @param halt the HttpResponse to use for the failure
     * @return the value inside this option, or a failed Future
     */
    def orHalt(halt: => HttpResponse): Future[A] = v match {
      case Some(a) => a.continue
      case None => Future.failed(new HaltException(halt))
    }

    /**
     * Return the value inside this option, or fail
     * @param status the response code to return on failure
     * @param entity the response body to return on failure, if any
     * @param headers headers to return on failure, if any
     * @return the value inside this option, or a failed Future
     */
    def orHaltWith(status: => StatusCode,
                   entity: => HttpEntity = Empty,
                   headers: => List[HttpHeader] = Nil): Future[A] = {
      orHalt(HttpResponse(status, entity, headers))
    }

    /**
     * Return the value inside this option, or fail with a 500 Internal Server Error
     * @param entity the response body to return on failure, if any
     * @param headers headers to return on failure, if any
     * @return the value inside this option, or a failed Future with a 500 error
     */
    def orError(entity: => HttpEntity = Empty,
                headers: => List[HttpHeader] = Nil): Future[A] = {
      orHaltWith(InternalServerError, entity, headers)
    }

  }

  /**
   * Implicit wrapper to allow right-biased, left Throwable `scala.util.Either` values to halt or throw
   *
   * {{{
   *   import com.paypal.cascade.http.resource._
   *   Right("hi").orErrorWithMessage                 // Future("hi")
   *   Left(new Throwable("fail")).orErrorWithMessage // Future(HaltException(500, "fail", List()))
   * }}}
   *
   * @param either the either to wrap
   * @tparam A the right type of the either
   */
  implicit class RichEitherThrowableHalt[A](either: Either[Throwable, A]) {

    /**
     * Return the value on the right, or throw a HaltException containing the message on the left
     * @param status the response code to return on left
     * @param f an optional function that can manipulate the left message
     * @return the value on the right
     * @throws HaltException containing the message on the left, if Left
     */
    @throws[HaltException]
    def orThrowHaltExceptionWithMessage(status: StatusCode)
                                       (f: Throwable => String = _.getMessage): A = {
      either.fold(
        e => throw HaltException(status, f(e)),
        a => a
      )
    }

    /**
     * Return the value on the right, or throw a HaltException containing the message on the left with
     * an error code of 500 Internal Server Error
     * @param f an optional function that can manipulate the left message
     * @return the value on the right
     * @throws HaltException containing the message on the left and a 500 error, if Left
     */
    @throws[HaltException]
    def orThrowHaltExceptionWithErrorMessage(f: Throwable => String = _.getMessage): A =
      orThrowHaltExceptionWithMessage(InternalServerError)(f)

    /**
     * Return the value on the right, or halt
     * @param status the response code to return on left
     * @param f an optional function that can manipulate the left message
     * @return the value on the right, or a failed Try with the left message
     */
    def orHaltWithMessage(status: StatusCode)
                         (f: Throwable => String = _.getMessage): Try[A] = either.fold(
      l => Failure(HaltException(status, f(l))),
      r => Success(r)
    )

    /**
     * Return the value on the right, or halt with the message on the left and an error code
     * of 500 Internal Server Error
     * @param f an optional function that can manipulate the left message
     * @return the value on the right, or a failed Try with the left message and a 500 error
     */
    def orErrorWithMessage(f: Throwable => String = _.getMessage): Try[A] = orHaltWithMessage(InternalServerError)(f)

  }

  /**
   * Implicit wrapper to allow [[scala.util.Try]] values to halt or throw.
   * Handled internally as a right-biased `scala.util.Either` with a Throwable left.
   *
   * {{{
   *   import com.paypal.cascade.http.resource._
   *   Try { "hi" }.orErrorWithMessage()                      // Future("hi")
   *   Try { throw new Throwable("no") }.orErrorWithMessage() // Future(HaltException(HttpResponse(500, "no", List())))
   * }}}
   *
   * @param t the try to wrap
   * @tparam A the success type of the try
   */
  implicit class RichTryHalt[A](t: Try[A]) extends RichEitherThrowableHalt(t.toEither)

  /**
   * Implicit wrapper to allow right-biased `scala.util.Either` values, of any left type, to halt or throw
   *
   * {{{
   *   import com.paypal.cascade.http.resource._
   *   Right("hi").orError                                                             // Future("hi")
   *   Left(CustomError("no")).orError { c => HttpResponse(500, c.getMessage, List()) } // Future(HaltException(...))
   * }}}
   *
   * @param either the either to wrap
   * @tparam T the left type, by convention representing a failure
   * @tparam A the right type, by convention representing a success
   */
  implicit class RichEitherHalt[T, A](either: Either[T, A]) {

    /**
     * Return the value on the right, or throw a halt exception containing a message based on the left value
     * @param haltFn a method to convert a left value to an HttpResponse
     * @return the value on the right
     * @throws HaltException containing a message based on the left, if Left
     */
    @throws[HaltException]
    def orThrowHaltException(haltFn: T => HttpResponse): A = {
      either.fold(
        t => throw new HaltException(haltFn(t)),
        a => a
      )
    }

    /**
     * Return the value on the right, or throw a HaltException containing a message based on the left value
     * and an error code of 500 Internal Server error
     * @param errorFn optionally, a method to convert a left value to an HttpEntity
     * @return the value on the right
     * @throws HaltException with a 500 error and, optionally, a message based on the left
     */
    @throws[HaltException]
    def orErrorNow(errorFn: T => HttpEntity = { _ => Empty }): A =
      orThrowHaltException(e => HttpResponse(InternalServerError, errorFn(e)))

    /**
     * Return the value on the right, or halt
     * @param haltFn a method to convert a left value to an HttpEntity
     * @return the value on the right, or a failed Future based on the left value
     */
    def orHalt(haltFn: T => HttpResponse): Future[A] = {
      either.fold(
        t => Future.failed[A](new HaltException(haltFn(t))),
        a => a.continue
      )
    }

    /**
     * Return the value on the right, or halt with an error code of 500 Internal Server Error
     * @param errorFn optionally, a method to convert a left value to an HttpEntity
     * @return the value on the right, or a failed Future with a 500 error and, optionally, a message based on the left
     */
    def orError(errorFn: T => HttpEntity = { _ => Empty }): Future[A] =
      orHalt(e => HttpResponse(InternalServerError, errorFn(e)))
  }

  /**
   * Implicit wrapper to allow Booleans to halt or throw
   *
   * {{{
   *   import com.paypal.cascade.http.resource._
   *   true.orError  // Try({})
   *   false.orError // Try(HaltException(HttpResponse(500, Empty, List())))
   * }}}
   *
   * @param v the boolean to wrap
   */
  implicit class RichBooleanTryHalt(v: Boolean) {

    /**
     * If false, halt
     * @param halt the HttpResponse to use for the failure
     * @return an empty successful Future, or a failed Future
     */
    def orHaltT(halt: => HttpResponse): Try[Unit] = {
      if (v) Success(()) else Failure(new HaltException(halt))
    }

    /**
     * If false, halt
     * @param status the response code to return on false
     * @param entity the response body to return on false, if any
     * @param headers headers to return on false, if any
     * @return an empty successful Future, or a failed Future
     */
    def orHaltWithT(status: => StatusCode,
                   entity: => HttpEntity = Empty,
                   headers: => List[HttpHeader] = Nil): Try[Unit] = {
      orHaltT(HttpResponse(status, entity, headers))
    }

    /**
     * If false, halt with an error code of 500 Internal Server Error
     * @param entity the response body to return on false, if any
     * @param headers headers to return on false, if any
     * @return an empty successful Future, or a failed Future with a 500 error
     */
    def orErrorT(entity: => HttpEntity = Empty,
                headers: => List[HttpHeader] = Nil): Try[Unit] = {
      orHaltWithT(InternalServerError, entity, headers)
    }

  }

  /**
   * Implicit wrapper to allow Booleans to halt or throw
   *
   * {{{
   *   import com.paypal.cascade.http.resource._
   *   true.orError  // Future({})
   *   false.orError // Future(HaltException(HttpResponse(500, Empty, List())))
   * }}}
   *
   * @param v the boolean to wrap
   */
  implicit class RichBooleanFutureHalt(v: Boolean) {

    /**
     * If false, halt
     * @param halt the HttpResponse to use for the failure
     * @return an empty successful Future, or a failed Future
     */
    def orHalt(halt: => HttpResponse): Future[Unit] = {
      if (v) ().continue else Future.failed(new HaltException(halt))
    }

    /**
     * If false, halt
     * @param status the response code to return on false
     * @param entity the response body to return on false, if any
     * @param headers headers to return on false, if any
     * @return an empty successful Future, or a failed Future
     */
    def orHaltWith(status: => StatusCode,
                   entity: => HttpEntity = Empty,
                   headers: => List[HttpHeader] = Nil): Future[Unit] = {
      orHalt(HttpResponse(status, entity, headers))
    }

    /**
     * If false, halt with an error code of 500 Internal Server Error
     * @param entity the response body to return on false, if any
     * @param headers headers to return on false, if any
     * @return an empty successful Future, or a failed Future with a 500 error
     */
    def orError(entity: => HttpEntity = Empty,
                headers: => List[HttpHeader] = Nil): Future[Unit] = {
      orHaltWith(InternalServerError, entity, headers)
    }

  }

  /**
   * Implicit wrapper to allow Futures to halt
   *
   * {{{
   *   import com.paypal.cascade.http.resource._
   *   Future { "hi" }.orHalt { case e: Throwable => HttpResponse(...) }
   *     // => Future("hi")
   *   Future { throw new Throwable("fail") }.orHalt { case e: Throwable => HttpResponse(...) }
   *     // => Future(HaltException(HttpResponse(...)))
   * }}}
   *
   * @param v the future to wrap
   * @tparam T the success type of the future
   */
  implicit class RichFuture[T](v: Future[T]) {

    /**
     * Maps an error in this future to a HaltException failure
     * @param halt a partial function yielding an HttpResponse for a failure
     * @param ec implicitly, the [[scala.concurrent.ExecutionContext]] of this future
     * @return a future with a HaltException failure, if a failure is encountered
     */
    def orHalt(halt: PartialFunction[Throwable, HttpResponse])(implicit ec: ExecutionContext): Future[T] = {
      v.recoverWith {
        halt.andThen { resp => Future.failed(new HaltException(resp)) }
      }
    }
  }

  /**
   * Implicit wrapper to allow anything to continue
   *
   * {{{
   *   import com.paypal.cascade.http.resource._
   *   "hi".continue  // Future("hi")
   * }}}
   *
   * @param v the object to wrap
   * @tparam T the type of this object
   */
  implicit class RichIdentity[T](v: => T) {

    /**
     * Wraps this object in a successful Future
     * @return the wrapped, successful Future
     */
    def continue: Future[T] = Future.successful(v)
  }

  /**
   * Implicit wrapper to allow any Throwable to halt
   *
   * {{{
   *   import com.paypal.cascade.http.resource._
   *   (new Throwable("no")).haltWith(InternalServiceError)()  // Future(HaltException(HttpResponse(500, "no", List())))
   * }}}
   *
   * @param t the throwable to wrap
   */
  implicit class RichThrowableHalt(t: Throwable) {

    /**
     * Wraps this Throwable in a HaltException, then in a failed Future
     * @param status the response code to return
     * @param f an optional function that can manipulate the throwable's message
     * @return the wrapped, failed Future
     */
    def haltWith(status: => StatusCode)(f: String => String = identity): Future[Unit] = {
      Future.failed(new HaltException(HttpResponse(status, f(t.getMessage))))
    }
  }

}
