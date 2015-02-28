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
package com.paypal.cascade.common

import com.paypal.cascade.common.option._

/**
 * Convenience methods and implicit wrappers for working with `scala.collection.Map`
 */
package object map {

  /**
   * Implicit wrapper for Map[A, B]
   * @param self the Map[A, B] to wrap
   */
  implicit class RichMap[A, B](self: Map[A, B]) {

    /**
     * Returns the map as an Option, None if the map is empty.
     * @return Some[Map[A, B]] if the map has items, and None otherwise.
     */
    def orNone: Option[Map[A, B]] =  self.some.filter(_.nonEmpty)
  }

}
