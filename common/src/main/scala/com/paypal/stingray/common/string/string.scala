package com.paypal.stingray.common

import com.paypal.stingray.common.constants.ValueConstants.charsetUtf8

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
      inner.getBytes(charsetUtf8)
    }
  }
}
