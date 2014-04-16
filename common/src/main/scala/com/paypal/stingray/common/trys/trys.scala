package com.paypal.stingray.common

import scala.util.{Failure, Success, Try, Either}
import scala.concurrent.Future

/**
 * Convenience wrappers and methods for working with [[scala.util.Try]].
 * Named `trys` so that it doesn't conflict with the `try` keyword (backticks on packages are unattractive).
 */
package object trys {

  /**
   * Implicit wrapper for Try objects
   * @param self the Try object to be wrapped
   * @tparam A the success type of the Try
   */
  implicit class RichTry[A](self: => Try[A]) {

    /**
     * Converts this `Try[A]` to an [[scala.util.Either]] with a Throwable Left type
     * @return an Either based on this Try
     */
    def toEither: Either[Throwable, A] = {
      try {
        Right(self.get)
      } catch {
        case e: Throwable => Left(e)
      }
    }

    /**
     * Converts this `Try[A]` to an [[scala.util.Either]] with an arbitrary Left type
     * @param f converts from a Throwable to an arbitrary type
     * @tparam LeftT the Left type to use
     * @return an Either based on this Try
     */
    def toEither[LeftT](f: Throwable => LeftT): Either[LeftT, A] = {
      try {
        Right(self.get)
      } catch {
        case e: Throwable => Left(f(e))
      }
    }

    /**
     * Converts this `Try[A]` to a [[scala.concurrent.Future]]
     *
     * @return a Future based on this Try
     */
    def toFuture: Future[A] = {
      self match {
        case Success(s) => Future.successful(s)
        case Failure(e) => Future.failed(e)
      }
    }
  }

}
