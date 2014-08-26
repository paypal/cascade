/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.common.tests.util

import org.specs2.matcher.ConcurrentExecutionContext
import scala.concurrent.ExecutionContext

/**
 * This trait can be used to deactive the implicit concurrent execution context.
 * Added in Specs 2.4 via https://github.com/etorreborre/specs2/commit/a165db45ba213f1794b3738ab996aad383a1ba0e.
 */
trait NoConcurrentExecutionContext extends ConcurrentExecutionContext {
  override val concurrentExecutionContext: ExecutionContext = concurrent.ExecutionContext.Implicits.global
}
