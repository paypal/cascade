package com.paypal.stingray.common

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Try
import org.slf4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 10/2/13
 * Time: 5:13 PM
 */
package object future {

  /**
   * a {{{scala.concurrent.ExecutionContext}}} that runs tasks immediately, and logs errors to the given logger.
   * this context is useful for use with mapping functions that are cheap to compute (ie: simple transformations, etc)
   * @param logger the logger to which to log errors
   * @return the new {{{ExecutionContext}}}
   */
  def sequentialExecutionContext(logger: Logger): ExecutionContext = new ExecutionContext {
    def reportFailure(t: Throwable) {
      logger.error(t.getMessage, t)
    }

    def execute(runnable: Runnable) {
      runnable.run()
    }
  }

  implicit class RichFutureHelpers[T](v: Future[T]) {

    /**
     * Converts a Future Failure Throwable into a different Throwable type
     * @param f the conversion function
     * @param ctx implicitly, the execution context of this Future
     * @return a Future of the same type `T`, with Failures mapped into a different Throwable type
     */
    def mapFailure(f: Throwable => Throwable)(implicit ctx: ExecutionContext): Future[T] = {
      v.transform(identity, f)
    }

    /**
     * Converts, via a partial function, a Future Failure Throwable into a different Throwable type
     * @param f the conversion partial function
     * @param ctx implicitly, the execution context of this Future
     * @tparam E the resulting Throwable type or types
     * @return a Future of the same type `T`, with Failures mapped into a different Throwable type
     */
    def mapFailure[E <: Throwable](f: PartialFunction[Throwable, E])(implicit ctx: ExecutionContext): Future[T] = {
      v.transform(identity, f.applyOrElse(_, identity[Throwable]))
    }

    /**
     * Blocks to get the result of a Future using Await.result, wrapped in a [[scala.util.Try]]
     * @param dur the maximum amount of time to block; default 1 second
     * @return a Try containing one of:
     *         the value of the Future;
     *         an exception of a failed Future, or;
     *         one of the three Throwable types from Await.result
     */
    def block(dur: Duration = 1.second): Try[T] = {
      Try(Await.result(v, dur))
    }

    /**
     * Blocks to get the result of a Future using Await.result; throws the exception inside of a failed Future,
     * and can throw one of the three Throwable types from Await.result
     * @param dur the maximum amount of time to block; default 1 second
     * @return the value of the Future
     * @throws InterruptedException if the current thread is interrupted while waiting
     * @throws TimeoutException if after waiting for the specified time `v` is still not ready
     * @throws IllegalArgumentException if `dur` is [[scala.concurrent.duration.Duration.Undefined Duration.Undefined]]
     */
    def blockUnsafe(dur: Duration = 1.second): T = {
      Await.result(v, dur)
    }

  }
}
