package com.paypal.stingray.common

import scala.util.{Success, Failure, Try}

/**
 * Convenience methods and implicits for working with [[scala.util.Either]].
 *
 * When working with Either, our convention is to right-bias correct values. That is, when working with an Either,
 * prefer to use `Either[Failure, Success]` as the pattern.
 *
 * Additionally, strongly prefer to use [[scala.util.Try]] over Either whenever a failure case is to be a Throwable.
 * In general practice, this should encompass the vast majority of cases.
 */
package object either {

  /**
   * Implicit wrapper to convert regular objects to [[scala.util.Either]]
   *
   * {{{
   *   import com.paypal.stingray.common.either._
   *   "hello".toRight[Throwable]               // Either[Throwable, String]
   *   "hello".toLeft[Int]                      // Either[String, Int]
   *   (new Throwable("error")).toLeft[String]  // Either[Throwable, String]
   * }}}
   *
   * @param self the wrapped object
   * @tparam A the type of the wrapped object
   */
  implicit class EitherOps[A](self: A) {
    /**
     * Wraps the object in a [[scala.util.Right]]
     * @tparam X the [[scala.util.Left]] type for the resulting Either
     * @return an Either containing the object as its Right
     */
    def toRight[X]: Either[X, A] = Right(self)

    /**
     * Wraps the object in a [[scala.util.Left]]
     * @tparam X the [[scala.util.Right]] type for the resulting Either
     * @return an Either containing the object as its Left
     */
    def toLeft[X]: Either[A, X] = Left(self)
  }

  /**
   * Implicit wrapper to convert Either[Throwable, A] objects to [[scala.util.Try]], right-biasing
   * @param either the wrapped object
   * @tparam A the Right type of the wrapped object
   */
  implicit class ThrowableEitherToTry[A](either: Either[Throwable, A]) {
    /**
     * Converts the object to a [[scala.util.Try]]
     * @return a Try with either the value on the right, or a failure based on the left
     */
    def toTry: Try[A] = either.fold(e => Failure(e), a => Success(a))
  }

  /**
   * Implicit wrapper to convert arbitrary Either objects to [[scala.util.Try]], right-biasing
   * @param either the wrapped object
   * @tparam E the Left type of the wrapped object
   * @tparam A the Right type of the wrapped object
   */
  implicit class AnyEitherToTry[E, A](either: Either[E, A]) {
    /**
     * Converts the object to a [[scala.util.Try]]
     * @param f the conversion function for Left values
     * @return a Try with either the value on the right, or a converted failure based on the left
     */
    def toTry(f: E => Throwable): Try[A] = either.fold(e => Failure(f(e)), a => Success(a))
  }

}
