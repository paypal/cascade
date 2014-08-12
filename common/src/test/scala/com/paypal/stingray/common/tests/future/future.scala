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
package com.paypal.stingray.common.tests

import com.paypal.stingray.common.trys._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
 * Convenience methods and implicits for working with Futures within tests.
 */
package object future {

  /** Used inside of [[RichTestFuture]] to control Await timing. By default, blocks infinitely; override if needed. */
  lazy val defaultAwaitDuration: Duration = Duration.Inf

  /**
   * Wrapper for Futures
   *
   * {{{
   *   val f = Future { ... }
   *   f.toTry
   * }}}
   *
   * @param f the Future to wrap
   * @tparam T the type of the Future
   */
  implicit class RichTestFuture[T](f: Future[T]) {

    /**
     * Blocks for a result on `f`, wrapping failures or a timeout in a Try
     * @param awaitFor Duration to await for the Future; by default, waits infinitely
     * @return a Try of the value, or a failure/timeout
     */
    def toTry(awaitFor: Duration = defaultAwaitDuration): Try[T] = Try { Await.result(f, awaitFor) }
    def toTry: Try[T] = this.toTry()

    /**
     * Blocks for a result on `f`, yielding Some if successful or None if not
     * @param awaitFor Duration to await for the Future; by default, waits infinitely
     * @return Some value if successful, None if not
     */
    def toOption(awaitFor: Duration = defaultAwaitDuration): Option[T] = toTry(awaitFor).toOption
    def toOption: Option[T] = this.toOption()

    /**
     * Blocks for a result on `f`, wrapping failures or a timeout in a right-biased Either
     * @param awaitFor Duration to await for the Future; by default, waits infinitely
     * @return a right-biased Either of the value, or a failure/timeout
     */
    def toEither(awaitFor: Duration = defaultAwaitDuration): Either[Throwable, T] = toTry(awaitFor).toEither
    def toEither: Either[Throwable, T] = this.toEither()
  }

  /**
   * Wrapper for Futures containing Try objects
   * @param f the Future to wrap
   * @tparam T the type of the Try inside the Future
   */
  implicit class RichTestFutureThrowable[T](f: Future[Try[T]]) {

    /**
     * Blocks for a result on `f`, wrapping a timeout in a Try, and flattens
     * @param awaitFor Duration to await for the Future; by default, waits infinitely
     * @return a Try of the value, or a failure/timeout
     */
    def toTry(awaitFor: Duration = defaultAwaitDuration): Try[T] = Try { Await.result(f, awaitFor) }.flatten
    def toTry: Try[T] = this.toTry()

    /**
     * Blocks for a result on `f`, yielding Some if successful or None if not
     * @param awaitFor Duration to await for the Future; by default, waits infinitely
     * @return Some value if successful, None if not
     */
    def toOption(awaitFor: Duration = defaultAwaitDuration): Option[T] = toTry(awaitFor).toOption
    def toOption: Option[T] = this.toOption()

    /**
     * Blocks for a result on `f`, wrapping failures or a timeout in a right-biased Either
     * @param awaitFor Duration to await for the Future; by default, waits infinitely
     * @return a right-biased Either of the value, or a failure/timeout
     */
    def toEither(awaitFor: Duration = defaultAwaitDuration): Either[Throwable, T] = toTry(awaitFor).toEither
    def toEither: Either[Throwable, T] = this.toEither()

  }

}
