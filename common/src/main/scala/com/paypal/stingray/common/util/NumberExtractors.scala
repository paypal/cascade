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
package com.paypal.stingray.common.util

/**
 * Convenience object for working with Int values.
 *
 * {{{
 *   import com.paypal.stingray.common.util.IntExtractor
 *
 *   "1234" match { case IntExtractor(a) => a; case _ => 0; } // res0: Int = 1234
 *
 *   "1234a" match { case IntExtractor(a) => a; case _ => 0; } // res1: Int = 0
 *
 *   "1234.23" match { case IntExtractor(a) => a; case _ => 0; } // res2: Int = 0
 * }}}
 *
 */
object IntExtractor {

  /**
   * Convert a String to an Int, optionally returning the Int value, or None if conversion failed
   * @param s the String to convert
   * @return optionally, an Int, or None if conversion failed
   */
  def unapply(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case _ : java.lang.NumberFormatException => None
    }
  }

}

/**
 * Convenience object for working with Long values
 *
 * {{{
 *   import com.paypal.stingray.common.util.LongExtractor
 *
 *   "1234" match { case LongExtractor(a) => a; case _ => 0L; } // res0: Long = 1234
 *
 *   "1234a" match { case LongExtractor(a) => a; case _ => 0L; } // res1: Long = 0
 *
 *   "1234.23" match { case LongExtractor(a) => a; case _ => 0L; } // res2: Long = 0
 * }}}
 */
object LongExtractor {

  /**
   * Convert a String to a Long, optionally returning the Long value, or None if conversion failed
   * @param s the String to convert
   * @return optionally, a Long, or None if conversion failed
   */
  def unapply(s: String): Option[Long] = {
    try {
      Some(s.toLong)
    } catch {
      case _ : java.lang.NumberFormatException => None
    }
  }

}
