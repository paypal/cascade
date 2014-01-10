package com.paypal.stingray.common.json

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 3/12/12
 * Time: 6:38 PM
 */

class JSONException(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(message: String) = this(message, null)
  def this(cause: Throwable) = this("", cause)
}
