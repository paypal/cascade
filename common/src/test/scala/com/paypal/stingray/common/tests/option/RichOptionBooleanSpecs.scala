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
package com.paypal.stingray.common.tests.option

import org.specs2.Specification
import com.paypal.stingray.common.option._
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for implicit [[com.paypal.stingray.common.option.RichOptionBoolean]]
 */
class RichOptionBooleanSpecs extends Specification { override def is = s2"""

  RichOptionBoolean adds helpful methods to Option[Boolean]

    orFalse should
      return false given Some(false)                                          ${OrFalse().someTrue}
      return true given Some(true)                                            ${OrFalse().someFalse}
      return false given None                                                 ${OrFalse().noneBoolean}

    orTrue should
      return false given Some(false)                                          ${OrTrue().someTrue}
      return true given Some(true)                                            ${OrTrue().someFalse}
      return true given None                                                  ${OrTrue().noneBoolean}

  """

  case class OrFalse() extends CommonImmutableSpecificationContext {
    def someTrue = {
      Some(true).orFalse must beTrue
    }
    def someFalse = {
      Some(false).orFalse must beFalse
    }
    def noneBoolean = {
      (None: Option[Boolean]).orFalse must beFalse
    }
  }

  case class OrTrue() extends CommonImmutableSpecificationContext {
    def someTrue = {
      Some(true).orTrue must beTrue
    }
    def someFalse = {
      Some(false).orTrue must beFalse
    }
    def noneBoolean= {
      (None: Option[Boolean]).orTrue must beTrue
    }
  }
}
