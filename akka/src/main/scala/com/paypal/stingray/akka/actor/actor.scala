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
package com.paypal.stingray.akka

import scala.util.Try
import akka.actor.Status

/**
 * Convenience methods and implicit wrappers for working with [[akka.actor.Actor]] classes
 */
package object actor {

  /**
   * Implicit wrapper for Try objects, with methods that interoperate with Actors
   *
   * {{{
   *   // given some ActorRef `actor`
   *   val t = Try { ... }
   *   actor ! t.orFailure
   * }}}
   *
   * {{{
   *   // given some ActorRef `actor`
   *   val t = Try { ... }
   *   actor ! t.orFailureWith(new Exception("it died!"))
   * }}}
   *
   * {{{
   *   // given some ActorRef `actor` and a custom exception type `case class CustomException(e: Exception)`
   *   val t = Try { ... }
   *   actor ! t.orFailureWith(CustomException(_))
   * }}}
   *
   * @param t the Try to wrap
   * @tparam A the type of the Try
   */
  implicit class TryOrFailure[A](t: Try[A]) {

    /**
     * Returns the value of the Try if successful, or a [[akka.actor.Status.Failure]] message wrapping the exception if failed
     * @return the value or a failure
     */
    def orFailure: Any = t.recover {
      case e: Exception => Status.Failure(e)
    }.get

    /**
     * Returns the value of the Try if successful, or a [[akka.actor.Status.Failure]] message wrapping a given exception if failed
     * @param e the given exception
     * @return the value or a failure with the given exception
     */
    def orFailureWith(e: Exception): Any = t.recover {
      case _: Exception => Status.Failure(e)
    }.get

    /**
     * Returns the value of the Try if successful, or a [[akka.actor.Status.Failure]] message wrapping the result of applying the
     * given function to the failure exception
     * @param f the function
     * @return the value or a failure with the converted exception
     */
    def orFailureWith(f: Exception => Exception): Any = t.recover {
      case e: Exception => Status.Failure(f(e))
    }.get

  }

  /**
   * Implicit wrapper for Either objects, right-biased, with methods that interoperate with actors.
   *
   * {{{
   *   // given some ActorRef `actor` and a custom exception type `case class CustomException(e: Exception)`
   *   actor ! Right("hello").orFailureWith(new Exception("fail"))  // sends "hello"
   *   actor ! Left("no").orFailureWith(new Exception("fail"))      // sends Status.Failure(Exception("fail"))
   *   actor ! Left("no").orFailureWith(CustomException(_))         // sends Status.Failure(Exception("no"))
   * }}}
   *
   * @param either the Either to wrap
   * @tparam E the left type of the Either
   * @tparam A the right type of the Either
   */
  implicit class EitherOrFailure[E, A](either: Either[E, A]) {

    /**
     * Returns the value on the right, or an Akka [[akka.actor.Status.Failure]] containing the given Exception,
     * ignoring the left entirely.
     * @param e the given Exception
     * @return the value on the right or a failure
     */
    def orFailureWith(e: Exception): Any = either.right.getOrElse(Status.Failure(e))

    /**
     * Returns the value on the right, or an Akka [[akka.actor.Status.Failure]] containing the Exception resulting
     * from applying the given function to the left. Preferred if the left must convey some information beyond
     * simply representing a failure.
     * @param f the function
     * @return the value on the right or a failure
     */
    def orFailureWith(f: E => Exception): Any = either.fold(
      e => Status.Failure(f(e)),
      identity
    )
  }

}
