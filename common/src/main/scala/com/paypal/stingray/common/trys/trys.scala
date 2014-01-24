package com.paypal.stingray.common

import scala.util.{Try, Either}

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
     * Converts this `Try[A]` to an [[scala.util.Either]]
     * @return an Either based on this Try
     */
    def toEither: Either[Throwable, A] = {
      try {
        Right(self.get)
      } catch {
        case e: Throwable => Left(e)
      }
    }
  }
}
