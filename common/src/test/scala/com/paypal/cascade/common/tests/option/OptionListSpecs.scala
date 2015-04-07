/**
 * Copyright 2013-2015 PayPal
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
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for implicit [[com.paypal.cascade.common.option.ListOption]] and [[com.paypal.cascade.common.option.OptionList]]
 */
class OptionListSpecs extends Specification { override def is = s2"""
  ListOption is a wrapper for List[Option[T]] types
  
    sequence should
      return Some[List[T]] when all Options are Some              ${ListOptionTest().returnsSome}
      return None when on or more Options are None                ${ListOptionTest().returnsNone}
      returns Some[List[T]] when the list is empty                ${ListOptionTest().returnsEmptySome}

  OptionList is a Wrapper for Option[List[T]] types

    orNil should
      return a full list when the Option is Some                  ${OptionListTest().returnsFullList}
      return an empty list when the Option is None                ${OptionListTest().returnsEmptyList}

"""

  trait Context extends CommonImmutableSpecificationContext {
    protected val someValue = Option(20)
    protected val someOtherValue = Option(25)
  }

  case class ListOptionTest() extends Context {
    def returnsSome = apply {
      List(someValue, someOtherValue).sequence must beEqualTo(Some(List(20, 25)))
    }

    def returnsNone = apply {
      List(someValue, None).sequence must beNone
    }

    def returnsEmptySome = apply {
      List().sequence must beEqualTo(Some(Nil))
    }
  }

  case class OptionListTest() extends Context {

    def returnsFullList = apply {
      Option(List(someValue)).orNil must beEqualTo(List(someValue))
    }

    def returnsEmptyList = apply {
      None.orNil must beEqualTo(Nil)
    }

  }
  


}
