package com.paypal.stingray.common

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.string
 *
 * User: aaron
 * Date: 5/8/13
 * Time: 4:15 PM
 */
package object string {
  implicit class RichString(inner: String) {
    /**
     * a convenience method for .getBytes("UTF-8")
     * @return an array of bytes from UTF-8 encoding
     */
    def getBytesUTF8: Array[Byte] = {
      inner.getBytes("UTF-8")
    }
  }
}
