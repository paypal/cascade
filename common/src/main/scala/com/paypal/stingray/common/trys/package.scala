package com.paypal.stingray.common

import scala.util.{Try, Either}

/**
 * Convenience wrappers and methods for working with `Try`s.
 * Named `trys` so that it doesn't conflict with the `try` keyword.
 */
package object trys {

  implicit class RichTry[A](self: => Try[A]) {
    def toEither: Either[Throwable, A] = {
      try {
        Right(self.get)
      } catch {
        case e: Throwable => Left(e)
      }
    }
  }
}