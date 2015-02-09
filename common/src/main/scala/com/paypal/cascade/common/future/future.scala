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
package com.paypal.cascade.common

import scala.concurrent._

/**
 * Convenience methods and implicits for working with Futures.
 */
package object future {

  /**
   * Implicits to provide slightly cleaner patterns for handling Futures
   *
   * {{{
   *   import com.paypal.cascade.common.future._
   *   val f = Future { ... }
   *   f.mapFailure { case e: SomeThrowable => ... }
   * }}}
   *
   * @param v the future
   * @tparam T the type of the future
   */
  implicit class RichFuture[T](v: Future[T]) {

    /**
     * Converts a Future Failure Throwable into a different Throwable type
     * @param f the conversion function
     * @param ctx implicitly, the execution context of this Future
     * @return a Future of the same type `T`, with Failures mapped into a different Throwable type
     */
    def mapFailure(f: Throwable => Throwable)(implicit ctx: ExecutionContext): Future[T] = {
      v.transform(identity, f)
    }

    /**
     * Converts, via a partial function, a Future Failure Throwable into a different Throwable type
     * @param f the conversion partial function
     * @param ctx implicitly, the execution context of this Future
     * @tparam E the resulting Throwable type or types
     * @return a Future of the same type `T`, with Failures mapped into a different Throwable type
     */
    def mapFailure[E <: Throwable](f: PartialFunction[Throwable, E])(implicit ctx: ExecutionContext): Future[T] = {
      v.transform(identity, f.applyOrElse(_, identity[Throwable]))
    }

    /**
     * Converts a Future of any type into a Future with no contained return value.  This is useful when you care about
     * the completion of a Future and it's success or failure but not the value of a success.
     * @param ctx implicitly, the execution context of this Future
     * @return a Future of type Unit.
     */
    def toUnit(implicit ctx: ExecutionContext): Future[Unit] = {
      v.map { _ =>
        ()
      }
    }

  }

}
