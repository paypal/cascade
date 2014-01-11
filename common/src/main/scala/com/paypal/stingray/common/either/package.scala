package com.paypal.stingray.common

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.either
 *
 * User: aaron
 * Date: 7/17/13
 * Time: 3:59 PM
 */
package object either {
  final class EitherOps[A](self: A) {
    def toRight[X]: Either[X, A] = Right(self)

    def toLeft[X]: Either[A, X] = Left(self)
  }
  implicit def toEitherOps[A](a: => A) = new EitherOps(a)

}
