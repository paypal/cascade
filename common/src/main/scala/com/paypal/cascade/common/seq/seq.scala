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
package com.paypal.cascade.common

/**
 * Convenience methods and implicit wrappers for working with [[scala.collection.Seq]]
 */
package object seq {

  /**
   * Implicit wrapper for [[scala.collection.Seq]] objects
   * @param seq the sequence to wrap
   * @tparam T the type of values in the sequence
   */
  implicit class RichSeq[T](seq: Seq[T]) {

    /**
     * An accessor interface to sequences, because `seq(i)` and `seq.apply(i)` are hard, apparently
     * @param i the index in the sequence to retrieve
     * @return the value at index `i`, or None
     */
    def get(i: Int): Option[T] = {
      if(i >= 0 && i < seq.length) {
        Some(seq(i))
      } else {
        None
      }
    }
  }

}
