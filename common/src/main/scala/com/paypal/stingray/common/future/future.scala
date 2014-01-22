package com.paypal.stingray.common

import scala.concurrent.{Await, ExecutionContext, Future}
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
     *
     * @param f
     * @param ctx
     * @return
     */
    def mapFailure(f: Throwable => Throwable)(implicit ctx: ExecutionContext): Future[T] = {
      v.transform(identity, f)
    }

    /**
     *
     * @param f
     * @param ctx
     * @tparam E
     * @return
     */
    def mapFailure[E <: Throwable](f: PartialFunction[Throwable, E])(implicit ctx: ExecutionContext): Future[T] = {
      v.transform(identity, f.applyOrElse(_, identity[Throwable]))
    }

    /**
     *
     * @param dur
     * @return
     */
    def block(dur: Duration = 1.second): Try[T] = {
      Try(Await.result(v, dur))
    }

    /**
     *
     * @param dur
     * @return
     */
    def blockUnsafe(dur: Duration = 1.second): T = {
      Await.result(v, dur)
    }

  }
}
