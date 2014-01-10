package com.paypal.stingray.concurrent

import scala.concurrent.ExecutionContext
import org.slf4j.Logger

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.concurrent.future
 *
 * User: aaron
 * Date: 7/16/13
 * Time: 4:50 PM
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
}
