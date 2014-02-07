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
   * @param t the Try to wrap
   * @tparam A the type of the Try
   */
  implicit class TryOrFailure[A](t: Try[A]) {

    /**
     * Returns the value of the Try if successful, or a [[Status.Failure]] message wrapping the exception if failed
     * @return the value or a failure
     */
    def orFailure: Any = t.recover {
      case e: Throwable => Status.Failure(e)
    }.getOrElse(Status.Failure(new Throwable("unknown error")))

    /**
     * Returns the value of the Try if successful, or a [[Status.Failure]] message wrapping a given exception if failed
     * @param e the given exception
     * @return the value or a failure with the given exception
     */
    def orFailureWith(e: Throwable): Any = t.getOrElse(Status.Failure(e))

  }

}
