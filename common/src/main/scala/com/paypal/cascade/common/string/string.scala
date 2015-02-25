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

import java.nio.charset.StandardCharsets.UTF_8

/**
 * Convenience methods and implicit wrappers for working with Strings
 */
package object string {

  /**
   * Implicit wrapper for Strings
   * @param inner the String to wrap
   */
  implicit class RichString(inner: String) {

    /**
     * A convenience method for .getBytes("UTF-8")
     * @return an array of bytes from UTF-8 encoding
     */
    def getBytesUTF8: Array[Byte] = {
      inner.getBytes(UTF_8)
    }
  }

}
