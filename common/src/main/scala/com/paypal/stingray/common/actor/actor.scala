package com.paypal.stingray.common

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
   *   actor ! t.orFailureWith(new Throwable("it died!"))
   * }}}
   *
   * {{{
   *   // given some ActorRef `actor` and a custom exception type `case class CustomException(e: Throwable)`
   *   val t = Try { ... }
   *   actor ! t.orFailureWith(CustomException(_))
   * }}}
   *
   * @param t the Try to wrap
   * @tparam A the type of the Try
   */
  implicit class TryOrFailure[A](t: Try[A]) {

    /**
     * Returns the value of the Try if successful, or a [[Status.Failure]] message wrapping the exception if failed
     * @return the value or a failure
     */
    def orFailure: Any = t.recover {
      case e: Exception => Status.Failure(e)
    }.get

    /**
     * Returns the value of the Try if successful, or a [[Status.Failure]] message wrapping a given exception if failed
     * @param e the given exception
     * @return the value or a failure with the given exception
     */
    def orFailureWith(e: Exception): Any = t.recover {
      case _: Exception => Status.Failure(e)
    }.get

    /**
     * Returns the value of the Try if successful, or a [[Status.Failure]] message wrapping the result of applying the
     * given function to the failure exception
     * @param f the function
     * @return the value or a failure with the converted exception
     */
    def orFailureWith(f: Exception => Exception): Any = t.recover {
      case e: Exception => Status.Failure(f(e))
    }.get

  }

}
