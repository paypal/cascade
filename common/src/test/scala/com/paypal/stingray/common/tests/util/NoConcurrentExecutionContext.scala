package com.paypal.stingray.common.tests.util

import org.specs2.matcher.ConcurrentExecutionContext
import scala.concurrent.ExecutionContext

/**
 * This trait can be used to deactive the implicit concurrent execution context.
 * Added in Specs 2.4 via https://github.com/etorreborre/specs2/commit/a165db45ba213f1794b3738ab996aad383a1ba0e.
 */
trait NoConcurrentExecutionContext extends ConcurrentExecutionContext {
  override val concurrentExecutionContext: ExecutionContext = concurrent.ExecutionContext.Implicits.global
}
