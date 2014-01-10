package com.paypal.stingray.common

import scala.concurrent.Future
import scalaz.Validation
import scalaz.syntax.validation._
import com.paypal.stingray.common.validation._

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 10/2/13
 * Time: 4:52 PM
 */
package object boolean {

  implicit class RichBooleanHelpers(b: Boolean) {

    /**
     * A helper making it easy to get a validation from a boolean
     * given success and failure cases, like scalaz's Boolean.option(a)
     * Does not catch thrown exceptions
     */
    def validation[E, A](whenTrue: => A)(whenFalse: => E): Validation[E, A] = {
      if(b) {
        whenTrue.success
      } else {
        whenFalse.fail
      }
    }

    /**
     * A helper making it easy to get a dummy future from a boolean
     * given success and failure cases, like scalaz's Boolean.option(a)
     */
    def future[E, A](whenTrue: => A)(whenFalse: => Throwable): Future[A] = {
      if(b) {
        validating(whenTrue).toFuture
      } else {
        Future.failed(validating(whenFalse).valueOr(identity))
      }
    }
  }
}
