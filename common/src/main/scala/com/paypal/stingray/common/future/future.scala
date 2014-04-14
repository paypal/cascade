package com.paypal.stingray.common

import scala.concurrent._
import org.slf4j.Logger

/**
 * Convenience methods and implicits for working with Futures.
 */
package object future {

  /**
   * An [[scala.concurrent.ExecutionContext]] that runs tasks immediately, and logs errors to the given logger.
   * This context is useful for mapping functions that are cheap to compute (ie: simple transformations, etc)
   * @param logger the logger to which to log errors
   * @return the new [[scala.concurrent.ExecutionContext]]
   */
  def sequentialExecutionContext(logger: Logger): ExecutionContext = new ExecutionContext {
    override def reportFailure(t: Throwable) {
      logger.error(t.getMessage, t)
    }
    override def execute(runnable: Runnable) {
      runnable.run()
    }
  }

  /**
   * Implicits to provide slightly cleaner patterns for handling Futures
   *
   * {{{
   *   import com.paypal.stingray.common.future._
   *   val f = Future { ... }
   *   f.mapFailure { case e: SomeThrowable => ... }
   * }}}
   *
   * @param v the future
   * @tparam T the type of the future
   */
  implicit class RichFuture[T](v: Future[T]) {

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

  }

}
