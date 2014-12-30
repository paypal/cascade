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
package com.paypal.cascade.common.tests.option

import org.specs2.Specification
import com.paypal.cascade.common.option._
import java.util.concurrent.atomic.AtomicInteger
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for implicit [[com.paypal.cascade.common.option.RichOption]]
 */

class RichOptionSpecs extends Specification { override def is = s2"""
  RichOption is a wrapper for Option[T] types

  orThrow should
    throw only if the contained option is None                        ${OrThrow().throwsOnlyIfNone}

  toFuture should
    yield a future with the options value if some                     ${ToFuture().returnsValue}
    yield a future with Throwable as its failure state if none        ${ToFuture().returnsThrowable}

  Convenience method none[T] on option works                          ${None().works}
  """

  trait Context extends CommonImmutableSpecificationContext {
    protected val someValue = 20
  }

  case class OrThrow() extends Context {
    def throwsOnlyIfNone = apply {
      val wrappedNone = Option.empty[Int]
      val wrappedSome = Option(someValue)
      val exception = new Exception(getClass.getCanonicalName)
      (wrappedNone.orThrow(exception) must throwA(exception)) and
      (wrappedSome.orThrow(exception) must not throwA(exception)) and
      (wrappedSome.orThrow(exception) must beEqualTo(someValue))
    }
  }

  case class ToFuture() extends Context {
    def returnsValue = apply {
      val wrappedSome = Option(someValue)
      val successState = wrappedSome.toFuture(new Exception("test exception"))
      successState must beEqualTo(someValue).await
    }

    def returnsThrowable = apply {
      val wrappedNone = Option.empty[Int]
      val ex = new Exception("test exception")
      val failureState = wrappedNone.toFuture(ex)
      val result = failureState.value.get
      result must beFailedTry[Int].withThrowable[Exception](ex.getMessage)
    }
  }

  case class None() extends Context {
    def works = apply {
      import com.paypal.cascade.common.option.{none => CascadeNone}
      val stringNone = CascadeNone[String]
      stringNone must beEqualTo(Option.empty[String])
    }
  }

}
