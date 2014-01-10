package com.paypal.stingray.common

import scalaz.effect.IO
import scalaz.syntax.validation._
import scalaz.syntax.monad._
import scalaz.Validation

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.io
 *
 * User: aaron
 * Date: 6/5/12
 * Time: 9:53 AM
 */

package object io {

  implicit class RichIO[T](i: IO[T]) {
    def toValidation: IO[Validation[Throwable, T]] = i.map(_.success[Throwable]).except(_.fail[T].pure[IO])
  }

}
