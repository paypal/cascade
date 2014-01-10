package com.paypal.stingray.common

import concurrent.{ExecutionContext, Future}
import com.paypal.stingray.common.validation._

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 10/2/13
 * Time: 5:13 PM
 */
package object future {

  /**
   * wraps the computation in a synchronous future, like validating.
   */
  def wrapInFuture[T](t: => T): Future[T] = {
    validating(t).toFuture
  }

  implicit class RichFutureHelpers[T](v: Future[T]) {

    /**
     * transform the failure in the future
     */
    def mapFailure(f: Throwable => Throwable)(implicit ctx: ExecutionContext): Future[T] = {
      v.transform(identity, f)
    }

    /**
     * transform the failure in the future
     */
    def mapFailure[E <: Throwable](f: PartialFunction[Throwable, E])(implicit ctx: ExecutionContext): Future[T] = {
      v.transform(identity, f.applyOrElse(_, identity[Throwable]))
    }

  }
}
