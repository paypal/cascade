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
package com.paypal.stingray.common.tests.util

import org.specs2.specification._
import org.specs2.execute.{Failure => SpecsFailure, Result => SpecsResult, AsResult}
import com.paypal.stingray.common.logging.LoggingSugar

/**
 * Allows a test package to specify some amount of pre- or post-processing for its test context.
 *
 * {{{
 *   class FooSpecs extends Specification {
 *
 *     ...
 *
 *     trait Context extends CommonImmutableSpecificationContext {
 *       override def before() {
 *         // set up variables or something
 *         // make some fake requests
 *       }
 *
 *       override def after() {
 *         // do some cleanup
 *         // run some more post-processing
 *       }
 *
 *       // more variables and methods follow here
 *     }
 *
 *     ...
 *
 *     object TheseTests {
 *       case class ThisTest() extends Context {
 *         // test logic goes here
 *       }
 *     }
 *   }
 * }}}
 */

trait CommonImmutableSpecificationContext extends Around with LoggingSugar {

  private lazy val logger = getLogger[CommonImmutableSpecificationContext]

  /**
   * Add logic here to be executed before running a test set
   */
  def before() { }

  /**
   * Add logic here to be executed after running a test set
   */
  def after() { }

  /**
   * This should not be overwritten, as it contains the logic that allows `before()` and `after()` to function
   */
  override def around[T : AsResult](t: => T): SpecsResult = {
    try {
      before()
      AsResult(t)
    } catch {
      case e: Throwable => {
        logger.error(e.getMessage, e)
        throw e
      }
    } finally {
      after()
    }
  }

  /**
   * Convenience method to generate a Specs Failure from a Throwable
   * @param t the Throwable
   * @return a Specs Failure from that Throwable
   */
  protected def logAndFail(t: Throwable): SpecsResult = {
    logger.warn(t.getMessage, t)
    SpecsFailure(s"failed with exception ${t.getClass.getCanonicalName} (${t.getMessage})")
  }

}
