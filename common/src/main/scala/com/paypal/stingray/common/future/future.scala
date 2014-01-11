package com.paypal.stingray.common

import concurrent.{ExecutionContext, Future}

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 10/2/13
 * Time: 5:13 PM
 */
package object future {

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
